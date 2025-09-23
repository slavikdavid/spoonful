package com.spoonful.spoonful.recipe.web;

import com.spoonful.spoonful.auth.SecurityUtils;
import com.spoonful.spoonful.files.FileStorageService;
import com.spoonful.spoonful.recipe.dto.RecipeCreateRequest;
import com.spoonful.spoonful.recipe.dto.RecipeUpdateRequest;
import com.spoonful.spoonful.recipe.dto.RecipePhotoResponse;
import com.spoonful.spoonful.recipe.model.Recipe;
import com.spoonful.spoonful.recipe.model.RecipeStep;
import com.spoonful.spoonful.recipe.model.Tag;
import com.spoonful.spoonful.recipe.repo.RecipeRepository;
import com.spoonful.spoonful.recipe.repo.TagRepository;
import com.spoonful.spoonful.user.User;
import com.spoonful.spoonful.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeRepository recipes;
    private final UserRepository users;
    private final TagRepository tags;
    private final FileStorageService storage;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.media.base-path:/media}")
    private String mediaBase;

    @PostMapping
    @Transactional
    public Recipe create(@Valid @RequestBody RecipeCreateRequest req) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        User author = users.findById(uid).orElseThrow();

        Recipe r = new Recipe();
        r.setAuthor(author);
        r.setTitle(req.title());
        r.setDescription(req.description());
        r.setPrepMinutes(req.prepMinutes());
        r.setCookMinutes(req.cookMinutes());

        List<RecipeStep> steps = new ArrayList<>();
        int i = 1;
        if (req.steps() != null) {
            for (String s : req.steps()) {
                if (s == null || s.isBlank()) continue;
                RecipeStep st = new RecipeStep();
                st.setRecipe(r);
                st.setStepNo(i++);
                st.setInstruction(s.trim());
                steps.add(st);
            }
        }
        r.setSteps(steps);

        if (req.tags() != null) {
            for (String raw : req.tags()) {
                String name = normalizeTag(raw);
                if (name == null) continue;
                Tag t = tags.findByNameIgnoreCase(name).orElseGet(() -> {
                    Tag nt = new Tag();
                    nt.setName(name);
                    return tags.save(nt);
                });
                r.getTags().add(t);
            }
        }

        return recipes.save(r);
    }

    @PatchMapping("/{id}")
    @Transactional
    public Recipe update(@PathVariable Long id, @Valid @RequestBody RecipeUpdateRequest req) {
        Recipe r = recipes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ensureAuthor(r);

        if (req.title() != null && !req.title().isBlank()) r.setTitle(req.title().trim());
        if (req.description() != null) r.setDescription(req.description());
        if (req.prepMinutes() != null) r.setPrepMinutes(req.prepMinutes());
        if (req.cookMinutes() != null) r.setCookMinutes(req.cookMinutes());

        if (req.steps() != null) {
            List<RecipeStep> steps = new ArrayList<>();
            int i = 1;
            for (String s : req.steps()) {
                if (s == null || s.isBlank()) continue;
                RecipeStep st = new RecipeStep();
                st.setRecipe(r);
                st.setStepNo(i++);
                st.setInstruction(s.trim());
                steps.add(st);
            }
            r.getSteps().clear();
            r.getSteps().addAll(steps);
        }

        if (req.tags() != null) {
            Set<Tag> newTags = new LinkedHashSet<>();
            for (String raw : req.tags()) {
                String name = normalizeTag(raw);
                if (name == null) continue;
                Tag t = tags.findByNameIgnoreCase(name).orElseGet(() -> {
                    Tag nt = new Tag();
                    nt.setName(name);
                    return tags.save(nt);
                });
                newTags.add(t);
            }
            r.getTags().clear();
            r.getTags().addAll(newTags);
        }

        return recipes.save(r);
    }

    @GetMapping("/mine")
    public Page<Recipe> mine(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return recipes.findByAuthorId(uid, pageable);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, Object> delete(@PathVariable Long id) {
        Recipe r = recipes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ensureAuthor(r);

        recipes.delete(r);
        // also remove photos directory
        storage.deleteDirectory("recipes/" + id);

        return Map.of("deleted", true);
    }

    @PostMapping(path = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public List<RecipePhotoResponse> uploadPhotos(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files
    ) {
        Recipe r = recipes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ensureAuthor(r);

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No files provided");
        }

        String subdir = "recipes/" + id;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            storage.save(subdir, f);
        }

        return listPhotos(id);
    }

    @GetMapping("/{id}/photos")
    public List<RecipePhotoResponse> listPhotos(@PathVariable Long id) {
        recipes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String subdir = "recipes/" + id;
        List<Path> files = storage.list(subdir);

        files.sort(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)));

        List<RecipePhotoResponse> out = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            out.add(toPhotoResponse(files.get(i), i));
        }
        return out;
    }

    @DeleteMapping("/{id}/photos/{filename}")
    @Transactional
    public Map<String, Object> deletePhoto(@PathVariable Long id, @PathVariable String filename) {
        Recipe r = recipes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ensureAuthor(r);

        String subdir = "recipes/" + id;
        boolean ok = storage.delete(subdir, filename);
        if (!ok) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");

        return Map.of("deleted", true, "filename", filename);
    }


    private void ensureAuthor(Recipe r) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (!Objects.equals(r.getAuthor().getId(), uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private String normalizeTag(String raw) {
        if (raw == null) return null;
        String name = raw.trim();
        return name.isEmpty() ? null : name;
    }

    private RecipePhotoResponse toPhotoResponse(Path absolutePath, int order) {
        String url = buildPublicUrl(absolutePath);
        return new RecipePhotoResponse(null, url, false, order);
    }

    private String buildPublicUrl(Path absolutePath) {
        Path root = Path.of(uploadDir).toAbsolutePath().normalize();
        Path rel = root.relativize(absolutePath.toAbsolutePath().normalize());
        return (mediaBase.endsWith("/") ? mediaBase.substring(0, mediaBase.length() - 1) : mediaBase)
                + "/" + rel.toString().replace('\\', '/');
    }
}

package com.epam.resourceservice.controller;

import com.epam.resourceservice.dto.MultipleResourceDto;
import com.epam.resourceservice.dto.Resource;
import com.epam.resourceservice.service.database.ResourceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author www.epam.com
 */
@Validated
@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceServiceImpl resourceServiceImpl;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Resource uploadResource(@RequestParam("resource") MultipartFile file) {
        return new Resource(resourceServiceImpl.upload(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ByteArrayResource> findResourceById(@PathVariable("id") Integer id) {
        var resource = resourceServiceImpl.findResource(id);
        return ResponseEntity.ok()
                .contentLength(resource.length)
                .header("Content-type", "audio/mp3")
                .header("Content-disposition", "attachment; fileName=\"" + id + "\"")
                .body(new ByteArrayResource(resource));
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.OK)
    public MultipleResourceDto deleteResources(@RequestParam("id") List<Integer> ids) {
        return resourceServiceImpl.deleteResources(ids);
    }

}

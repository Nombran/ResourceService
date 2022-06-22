package com.epam.resourceservice.service.database;

import com.epam.resourceservice.dto.MultipleResourceDto;
import com.epam.resourceservice.exception.NotMp3FileException;
import com.epam.resourceservice.exception.ResourceNotFoundException;
import com.epam.resourceservice.model.Resource;
import com.epam.resourceservice.repository.ResourceRepository;
import com.epam.resourceservice.service.s3.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.amazonaws.services.mediaconvert.model.AudioCodec.MP3;

/**
 * @author www.epam.com
 */
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final FileUploadService fileUploadService;
    private final ResourceRepository repository;

    public int upload(MultipartFile resource) {
        if(!validateIfMp3(resource)) {
            throw new NotMp3FileException("Provided resource is not of .mp3 format");
        }
        var filePath = fileUploadService.uploadFile(resource);
        var resourceEntity = new Resource();
        resourceEntity.setLocation(filePath);
        return repository.save(resourceEntity).getId();
    }

    @SneakyThrows
    public byte[] findResource(int id) {
        var filePath = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Resource with id = " + id + " not found")).getLocation();
        return fileUploadService.findFile(filePath);
    }

    @Transactional
    public MultipleResourceDto deleteResources(final List<Integer> ids) {
        var resources = ids.stream()
                .map(id -> repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Resource with id = " + id + " doesn't exist")))
                .toList();
        resources.forEach(this::deleteResource);
        var response = new MultipleResourceDto();
        response.setIds(resources.stream().map(Resource::getId).toList());
        return response;
    }

    private void deleteResource(final Resource resource) {
        repository.delete(resource);
        fileUploadService.deleteFile(resource.getLocation());
    }

    private boolean validateIfMp3(final MultipartFile resource) {
       return MP3.toString().equalsIgnoreCase(FilenameUtils.getExtension(resource.getResource().getFilename()));
    }
}

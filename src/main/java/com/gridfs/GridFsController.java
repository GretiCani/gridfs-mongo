package com.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

@RestController
public class GridFsController {
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations operations;

    @Autowired
    private GridFSBucket gridFSBucket;


    @PostMapping("file/upload")
    public GridFsResponse upload(@RequestParam("file") MultipartFile file) {
        DBObject metaData = new BasicDBObject();
        System.err.println(file.getContentType());
        String fileName = String.valueOf(System.currentTimeMillis());
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            gridFsTemplate.store(inputStream, fileName,file.getContentType(), metaData);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return new GridFsResponse(fileName);
    }

    @GetMapping(value = "file/download/{fileName}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable("fileName") String fileName) throws IOException {
        if (fileName == null) {
            return null;
        }
        GridFSFindIterable result = operations.find(query(whereFilename().is(fileName)));
        GridFSFile gridFSFile = result.first();
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE,gridFSFile.getMetadata().getString("_contentType"));

        return new ResponseEntity <byte[]> ( IOUtils.toByteArray(gridFsResource.getInputStream()), headers, HttpStatus.OK);
    }

}
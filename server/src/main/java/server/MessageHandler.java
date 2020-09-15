package server;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import requests.DownloadRequest;
import requests.FileTreeRequest;
import requests.Response;
import requests.UploadRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class MessageHandler extends ChannelHandlerAdapter {

    private final Path rootPath;

    @SneakyThrows
    public MessageHandler(String path) {
        Path rootPath = Paths.get(path);
        if (Files.exists(rootPath)) {
            Files.createDirectories(rootPath);
        }
        this.rootPath = rootPath;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object request) {
        if (request instanceof UploadRequest) {
            handleUploadRequest(ctx, (UploadRequest) request);
        } else if (request instanceof DownloadRequest) {
            handleDownloadRequest(ctx, (DownloadRequest) request);
        } else if (request instanceof FileTreeRequest) {
            handleFileTreeRequest(ctx, (FileTreeRequest) request);
        } else {
            ctx.writeAndFlush
                    (Response.builder()
                            .requestId(UUID.fromString("-1"))
                            .status(Response.STATUS.ERROR)
                            .error("Unsupported request type")
                            .build());
        }
    }

    private void handleFileTreeRequest(ChannelHandlerContext ctx, FileTreeRequest request) {
        try {
            String username = request.getUser();
            Path userDir = rootPath.resolve(username);
            Map<String, String> files = Files.walk(userDir)
                    .collect(Collectors.toMap(p -> p.getParent().toString(), p -> p.getFileName().toString()));

            TreeMap<String, String> fileTree = new TreeMap<>(files);
            ctx.writeAndFlush(
                    Response.builder()
                            .requestId(request.getId())
                            .status(Response.STATUS.SUCCESS)
                            .fileTree(fileTree)
                            .build());
        } catch (IOException e) {
            log.error("Unable to get file tree", e);
            ctx.writeAndFlush(
                    Response.builder()
                            .requestId(request.getId())
                            .status(Response.STATUS.ERROR)
                            .error("Unable to get file tree")
                            .build());
        }
    }

    private void handleDownloadRequest(ChannelHandlerContext ctx, DownloadRequest request) {
        String username = request.getUser();
        Path filePath = rootPath.resolve(username).resolve(request.getPath());
        try {
            if (Files.exists(filePath)) {
                ctx.writeAndFlush(
                        Response.builder()
                                .requestId(request.getId())
                                .status(Response.STATUS.SUCCESS)
                                .path(filePath.toString())
                                .data(Files.readAllBytes(filePath))
                                .build());
                log.info(String.format("User: %s, File: %s, Result: downloading successful.", username, filePath));
            } else {
                ctx.writeAndFlush(
                        Response.builder()
                                .requestId(request.getId())
                                .status(Response.STATUS.ERROR)
                                .error("File doesn't exists")
                                .build());
            }
        } catch (IOException e) {
            log.error("FIle downloading is unsuccessful by error", e);
            ctx.writeAndFlush(
                    Response.builder()
                            .requestId(request.getId())
                            .status(Response.STATUS.ERROR)
                            .error("FIle downloading is unsuccessful")
                            .build());
        }
    }

    private void handleUploadRequest(ChannelHandlerContext ctx, UploadRequest request) {
        try {
            String username = request.getUser();
            Path userDir = rootPath.resolve(username);
            if (!Files.exists(userDir)) {
                Files.createDirectories(userDir);
            }

            Path fileName = userDir.resolve(request.getPath());
            byte[] data = request.getData();
            Files.write(fileName, data);

            ctx.writeAndFlush(
                    Response.builder()
                            .requestId(request.getId())
                            .status(Response.STATUS.SUCCESS)
                            .build());

            log.info(String.format("User: %s, File: %s, Result: uploading successful.", username, fileName));

        } catch (IOException e) {
            log.error("File uploading is unsuccessful by error", e);
            ctx.writeAndFlush(
                    Response.builder()
                            .requestId(request.getId())
                            .status(Response.STATUS.ERROR)
                            .error("File uploading is unsuccessful")
                            .build());
        }
    }
}

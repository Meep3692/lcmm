package ca.awoo.lcmm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

public class Download implements Publisher<Progress> {
    private final URL url;

    private final Progress progress;

    public Download(URL url) {
        this.url = url;
        progress = new Progress("Download: " + url.getFile(), 0, "Not started yet");
    }

    public CompletableFuture<File> download(File destination) {
        return CompletableFuture.supplyAsync(() -> {
            try{
                progress.update("Connecting");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    progress.update("Connected");
                    InputStream remoteFile = connection.getInputStream();
                    long length = connection.getContentLengthLong();
                    long total = 0;
                    progress.update("Downloading");
                    FileOutputStream localFile = new FileOutputStream(destination);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = remoteFile.read(buffer)) != -1) {
                        total += bytesRead;
                        localFile.write(buffer, 0, bytesRead);
                        progress.update("Downloading", (double) total / length, Progress.Status.RUNNING);
                    }
                    localFile.close();
                    remoteFile.close();
                    progress.update("Downloaded", 1, Progress.Status.FINISHED);
                    return destination;
                } else {
                    progress.setTask("Server returned " + responseCode);
                    progress.setStatus(Progress.Status.FAILED);
                    return null;
                }
            } catch (Exception e) {
                progress.setTask(e.getMessage());
                progress.setStatus(Progress.Status.FAILED);
                return null;
            }
        });
    }

    public Progress getProgress() {
        return progress;
    }

    @Override
    public void subscribe(Subscriber<? super Progress> subscriber) {
        progress.subscribe(subscriber);
    }
}

package gr.aueb.dmst.dockerWatchdog;

import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

public class MyImage {

    private final String name;
    private final String id;
    private final Long size;
    private String status;

    public MyImage(String name,String id, Long size,String status) {
        this.name = name;
        this.id = id;
        this.size = size;
        this.status = status;
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for size
    public Long getSize() {
        return size;
    }

    public static int countUsedImages(DockerClient dockerClient) {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        List<String> usedImageIds = containers.stream().map(Container::getImageId).distinct().toList();

        return usedImageIds.size();
    }

    @Override
    public String toString() {
        return "name = " + name + ",id = "+ id.substring(7) +  ", size = " + size + ", status = " + status ;
    }

    public String getName() {
        return name;
    }

    public String getStatus(){
        return this.status;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
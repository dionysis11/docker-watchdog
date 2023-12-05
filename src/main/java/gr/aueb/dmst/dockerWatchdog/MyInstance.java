package gr.aueb.dmst.dockerWatchdog;

import java.util.Map;

public class MyInstance {
    private final String id; // ID of instance is final
    private String name; // Instance name
    private long size; // Size of instance
    private Map < String, String > labels; // Labels of instance
    private final String image; // Image of instance
    private String status; // Status of instance
    private long pids; // PIDs of instance
    private long memoryUsage; // Memory usage of instance in MB
    private double cpuUsage; // CPU usage of instance in %
    private double blockI; // Block I of instance in MB
    private double blockO; // Block O of instance in MB

    // Constructor
    public MyInstance(String id, String name, String image, String status, Map < String, String > labels, long size, double cpuUsage, long memoryUsage, long pids, double blockI, double blockO) {

        // Initialize instance variables with the values of the parameters
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.labels = labels;
        this.size = size;
        this.memoryUsage = memoryUsage;
        this.pids = pids;
        this.cpuUsage = cpuUsage;
        this.blockI = blockI;
        this.blockO = blockO;
    }

    // Method toString that returns a string with the values of the instance variables
    @Override
    public String toString() {
        return "Name = " + name.substring(1) + " , ID = " + id + ", " + " , Image = " + image +
                " , Status = " + status + " , CPU Usage: " + String.format("%.2f", cpuUsage * 100) + " %" + " , Memory usage : " + String.format("%.2f", (double) memoryUsage) + " MB" + " , PIDs : " + pids + " , Block I/0 : " + String.format("%.2f", blockI) + "MB/" + String.format("%.2f", blockO) + "MB";
    }

    // Getter for id
    public String getId() {
        return id;
    }

    // Getter for image
    public String getImage() {
        return image;
    }

    // Getter for status
    public String getStatus() {
        return status;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Getter for labels
    public Map < String, String > getLabels() {
        return labels;
    }

    // Getter for size
    public long getSize() {
        return size;
    }

    // Getter for memoryUsage
    public long getMemoryUsage() {
        return memoryUsage;
    }

    // Getter for cpuUsage
    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    // Getter for PIDs
    public long getPids() {
        return pids;
    }

    // Getter for blockI
    public double getBlockI() {
        return blockI;
    }

    // Getter for blockO
    public double getBlockO() {
        return blockO;
    }

    // Setter for cpuUsage
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    // Setter for name
    public void setName(String newName) {
        this.name = newName;
    }

    // Setter for image
    public void setStatus(String status) {
        this.status = status;
    }

    // Setter for labels
    public void setLabels(Map < String, String > labels) {
        this.labels = labels;
    }

    // Setter for size
    public void setSize(long size) {
        this.size = size;
    }

    // Setter for PIDs
    public void setPids(long pids) {
        this.pids = pids;
    }

    // Setter for blockI
    public void setBlockI(double blockI) {
        this.blockI = blockI;
    }

    // Setter for blockO
    public void setBlockO(double blockO) {
        this.blockO = blockO;
    }

    // Given an ID of an instance, return the instance
    public static MyInstance getInstanceByid(String id) {
        MyInstance instanceToReturn = null;
        for (MyInstance instance: Main.myInstancesList) {
            if (id.equals(instance.getId())) {
                instanceToReturn = instance;
            }
        }
        if (instanceToReturn != null) {
            return instanceToReturn;
        } else {
            return null;
        }
    }
}
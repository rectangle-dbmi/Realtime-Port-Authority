package com.rectanglel.patstatic.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract class that has logic that helps with deciding to download from disk or from internet
 * <p>
 * Created by epicstar on 3/12/17.
 * @author Jeremy Jao
 */

public abstract class AbstractDataManager<T> {

    private final ReentrantReadWriteLock rwl;
    private final File dataDirectory;
    private final SourceOfTruth sourceOfTruth;
    private Type serializedType;

    public AbstractDataManager(File dataDirectory, SourceOfTruth sourceOfTruth, Type serializationType) {
        rwl = new ReentrantReadWriteLock();
        this.dataDirectory = new File(dataDirectory, getCacheFolderName());
        //noinspection ResultOfMethodCallIgnored
        this.dataDirectory.mkdirs();
        this.sourceOfTruth = sourceOfTruth;
        this.serializedType = serializationType;

    }

    protected abstract String getCacheFolderName();

    protected File getDataDirectory() {
        return dataDirectory;
    }

    private ReentrantReadWriteLock.ReadLock getReadLock() {
        return rwl.readLock();
    }

    private ReentrantReadWriteLock.WriteLock getWriteLock() {
        return rwl.writeLock();
    }

    /**
     * This is a lazy load of a Type.
     *
     * @return serialization type for the {@link com.google.gson.Gson} serializer
     */
    private Type getSerializedType() {
        return serializedType;
    }

    /**
     * Save the object to disk as JSON into the file
     * @param obj the object to save
     * @param file the file to save to
     * @throws IOException if the serialization fails
     */
    protected void saveAsJson(Object obj, File file) throws IOException {
        getWriteLock().lock();
        FileWriter fileWriter = new FileWriter(file);
        JsonWriter writer = new JsonWriter(fileWriter);
        try {
            Gson gson = getGson();
            gson.toJson(obj, getSerializedType(), writer);
        } finally {
            writer.flush();
            fileWriter.close();
            writer.close();
            getWriteLock().unlock();
        }
    }

    protected T getFromDisk(File file) throws IOException {
        // file exists... get from disk
        if (file.exists()) {
            getReadLock().lock();
            FileReader fileReader = new FileReader(file);
            JsonReader jsonReader = new JsonReader(fileReader);
            try {
                return getGson().fromJson(jsonReader, getSerializedType());
            } finally {
                fileReader.close();
                jsonReader.close();
                getReadLock().unlock();
            }
        }
        // if file doesn't exist, save bundled file from installation to disk
        String fileName = String.format("%s/%s", getCacheFolderName(), file.getName());
        copyStreamToFile(sourceOfTruth.getInputStreamForFileName(fileName), file);
        return getFromDisk(file);
    }

    protected void copyStreamToFile(InputStreamReader reader, File file) throws IOException {
        getWriteLock().lock();
        FileWriter fileWriter = new FileWriter(file);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            char[] buffer = new char[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                fileWriter.write(buffer, 0, length);
            }
        } finally {
            reader.close();
            fileWriter.close();
            getWriteLock().unlock();
        }

    }

    protected Gson getGson() {
        return new GsonBuilder().create();
    }

}

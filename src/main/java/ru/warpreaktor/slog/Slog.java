package ru.warpreaktor.slog;

import ru.warpreaktor.dictionaries.Status;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Кастомный класс для простого логирования в файл.
 */
public class Slog {
    // 1 условная единица. Число в байтах. В данном случае это мегабайт (1048576 байта)
    private final long mib = 1048576;
    private long maxSizeLogFile = 3l * mib;
    // Имя файла с логом по умолчанию.
    private String defaultFileName = "profiling.txt";
    // Директория в которой будет создан файл с логами.
    private String logFilePath = "/tmp/share/";
    // Формат вывода времени в файл логгирования.
    private String formatter = "dd-MM-yyyy HH:mm:ss";

    public Slog() {
    }

    /**
     * @param maxSizeLogFile - Максимальный размер файла в байтах.
     * @param logFilePath - Путь (директория) с лог-файлом.
     */
    public Slog(long maxSizeLogFile, String logFilePath) {
        this.maxSizeLogFile = maxSizeLogFile * mib;
        this.logFilePath = logFilePath;
    }

    public void info(String message){
        log(message, Status.INFO);
    }

    public void warn(String message){
        log(message, Status.WARN);
    }

    public void error(String message){
        log(message, Status.ERROR);
    }

    /**
     * @param message - сообщение для записи в лог-файл.
     */
    private void log(String message, Status status){
        flushMessage(LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatter)) + "---" + "[" + status.name() + "]" + message);
    }

    /**
     * Внутренний метод для записи сообщения в лог-файл
     * @param message
     */
    private void flushMessage(String message){
        try {
            Path path = new File(logFilePath + defaultFileName).toPath();

            if (!Files.exists(path)){
                Files.createFile(path);
            }
            if (Files.size(path) >= maxSizeLogFile){
                archivator(path);
            }
            Files.write(path, (message + "\r\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(String.format("failed to write to file %s; %s; %s",
                    logFilePath, e.getClass(), e.getCause()));
        }
    }

    private void archivator(Path archivingFile){
        try {
            Path archivedDir = new File(logFilePath + "/archived").toPath();
            if (!Files.exists(archivedDir)){
                Files.createDirectory(archivedDir);
            }
            Path archivedFile = new File(archivedDir + "/" + LocalDate.now() + "-" + defaultFileName).toPath();
            if (!Files.exists(archivedFile)){
                Files.move(archivingFile, archivedFile);
            }else{
                moveIfExists(archivingFile, archivedFile);
            }

        } catch (IOException e) {
            System.err.println(String.format("failed to archive file %s; %s; %s",
                    archivingFile, e.getClass(), e.getCause()));
        }
    }

    private void moveIfExists(Path movingFile, Path movedFile){
        int iteration = 1;
        iterationMove(movingFile, movedFile, iteration++);
    }

    private void iterationMove(Path movingFile, Path movedFile, int iteration){
        try {
            Path movedFilePath = new File(movedFile.getParent() + "/" + LocalDate.now() + "-" + "("+ iteration + ")" + defaultFileName).toPath();
            Files.move(movingFile, movedFilePath);
        } catch (IOException e) {
            System.err.println(String.format("failed to move file %s to %s; %s; %s",
                    movingFile, movedFile, e.getClass(), e.getCause()));
        }
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }

}

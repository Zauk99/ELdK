package com.diariokanto.web.dto;

import java.util.List;

// Clase simple para leer el JSON de la API sin líos de Spring Data
public class PaginaRespuesta<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;

    // Getters y Setters necesarios
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
}
package com.bitso;

public enum BitsoSort {
    ASC, DESC;
    
    public String toString() {
        return this.name().toLowerCase();
    }
}

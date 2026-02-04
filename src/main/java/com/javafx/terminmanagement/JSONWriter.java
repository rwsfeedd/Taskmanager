package com.javafx.terminmanagement;

import com.google.gson.stream.JsonWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class JSONWriter {
    private JsonWriter jsonWriter;

    public JSONWriter(JsonWriter jsonWriter) throws IOException{
        this.jsonWriter = jsonWriter;
        jsonWriter.setIndent("    ");
    }

    public void writeStringArray(String listName, List<String> stringList)  throws IOException{
        //listName als Enum übergaben und anschließend in switch statement resolven
        jsonWriter.name(listName);
        jsonWriter.beginArray();
        for(String string: stringList) jsonWriter.value(string);
        jsonWriter.endArray();
       //writeTodoArray
       //writePlanArray
    }

    public void writeDate(LocalDate date) throws IOException {
        jsonWriter.name("planDate");
        jsonWriter.value(Model.dateFormat.format(date));
    }
}

package de.skuzzle.enforcer.restrictimports;

public class DataImporter {

    public int importData(String[] inputData){
        int importedLines;
        for(String line : inputData){
            doImport(line);
            importedLines++;
        }
        return importedLines;
    }

}

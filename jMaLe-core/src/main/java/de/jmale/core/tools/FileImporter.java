package de.jmale.core.tools;

import de.jmale.core.data.DataSet;
import java.io.IOException;

/**
 * Interface which provides functionalities to import data from files.
 * 
 * @author Christian Vogel (<a href="mailto:contact@christian-voge.info">contact@christian-voge.info</a>)
 * @version 1.0.0 2012-05-13
 * @since 1.0.0
 */
public interface FileImporter {
    /**
     * Imports data from a given file and maps them, if possible, to {@link DataSet}
     * object.
     * @param filename identifies the file to be read
     * @return a set containing data extracted from the file
     * @throws IOException will be thrown if an error occured during the read
     *  process
     */
    public DataSet doImport(String filename) throws IOException;
    /**
     * Imports data from a given file and maps them, if possible, to {@link DataSet}
     * object.          
     * @param file the file to be read
     * @return a set containing data extracted from the file
     * @throws IOException will be thrown if an error occured during the read
     *  process
     */
    public DataSet doImport(java.io.File file) throws IOException;
}

package de.jmale.core.tools;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.jmale.core.data.DataSet;
import java.io.*;

/**
 * This class represents a special CSV file format importer. A CSV file contains
 * tuples where each one has character seperated values. In some cases it is also
 * possible to provide header definition and should be always placed in the first
 * line of the file.
 * 
 * @author Christian Vogel (<a href="mailto:contact@christian-voge.info">contact@christian-voge.info</a>)
 * @version 1.0.0 2012-03-29
 * @since 1.0
 */
public class CSVFileImporter implements FileImporter {
    /**
     * Default seperator used to split the attributes in a CSV file.
     */
    public static final String CSV_DEFAULT_ATTRIBUTE_SEPERATOR = ";";

    private final String seperator;
    private final boolean withHeader;

    /**
     * Default constructor which initialize a new {@code CSVReader} with
     * default values.
     * <p>
     * Default: 
     * <ul>
     *  <li>seperator: ';'</li>
     *  <li>file contains header information: false</li>
     * </ul>
     * 
     * @see CSVReader#CSV_DEFAULT_ATTRIBUTE_SEPERATOR
     */
    public CSVFileImporter() {
        this(CSV_DEFAULT_ATTRIBUTE_SEPERATOR);
    }

    /**
     * Constructor which initialize a new {@code CSVReader} with a given
     * seperator and some default values.
     * <p>
     * Defaults:
     * <ul>
     *  <li>file contains header information: false</li>
     * </ul>
     * 
     * @param seperator identifies the attribute seperator in a CSV file 
     */
    public CSVFileImporter(final String seperator) {
        this(seperator,false);
    }

    /**
     * Constructor which initialize a new {@code CSVReader} with a given
     * seperator and whether the CSV file contains header information or
     * not.
     * 
     * @param seperator identifies the attribute seperator in a CSV file
     * @param withHeader identifies whether file contains header information
     */
    public CSVFileImporter(final String seperator, final boolean withHeader) {
        this.seperator = seperator;
        this.withHeader = withHeader;
    }

    /**
     * {@inheritDoc}
     * <p>
     * All attributes should only contain double values, except the header row. If
     * values cannot be parsed to double, they will not be considered.
     * 
     * @throws IllegalArgumentException will be thrown if parameter is null or
     *  empty
     */
    public final DataSet doImport(String filename) throws IOException {
        if(Strings.isNullOrEmpty(filename)) {
            throw new IllegalArgumentException("filename cannot be empty nor null");
        }

        return this.doImport(new File(filename));
    }

    /**
     * {@inheritDoc}
     * <p>
     * All attributes should only contain double values, except the header row. If
     * values cannot be parsed to double, they will not be considered and set to
     * zero.
     * 
     * @throws NullPointerException will be thrown if parameter is not provided
     */
    public final DataSet doImport(File file) throws IOException {
        Preconditions.checkNotNull(file);

        if(!file.exists()) {
            throw new FileNotFoundException("file " + file.getName() + " not found");
        }

        if(!file.isFile()) {
            throw new java.io.IOException("provided file is not a file");
        }

        FileInputStream fstream = new FileInputStream(file);

	DataInputStream in = new DataInputStream(fstream);
	BufferedReader br = new BufferedReader(new InputStreamReader(in));

	boolean ignoreFirstLine = withHeader;

        String strLine;
	while ((strLine = br.readLine()) != null)   {
            String[] columns = strLine.split(seperator);
	    double[] attributes = new double[columns.length];

            // TODO: adding attributes to data instance
	}

        throw new UnsupportedOperationException("Not supported yet.");
    }
}
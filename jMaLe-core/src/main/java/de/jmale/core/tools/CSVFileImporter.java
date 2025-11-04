package de.jmale.core.tools;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.jmale.core.data.DataSet;
import de.jmale.core.observability.MetricsCollector;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
    private static final Logger logger = LoggerFactory.getLogger(CSVFileImporter.class);
    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("jmale.csv.importer");

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
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("csv.import.by_filename")
            .setAttribute("file.name", filename != null ? filename : "null")
            .startSpan();

        try {
            MDC.put("operation", "csv.import");
            MDC.put("file.name", filename);

            logger.debug("Starting CSV import from filename: {}", filename);

            if(Strings.isNullOrEmpty(filename)) {
                logger.error("CSV import failed: filename is null or empty");
                throw new IllegalArgumentException("filename cannot be empty nor null");
            }

            DataSet result = this.doImport(new File(filename));

            long duration = System.currentTimeMillis() - startTime;
            logger.info("CSV import completed successfully in {} ms", duration);

            try {
                MetricsCollector.getInstance().recordOperation("csv.import", duration);
            } catch (IllegalStateException e) {
                logger.debug("MetricsCollector not initialized, skipping metrics recording");
            }

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("CSV import failed after {} ms", duration, e);
            span.recordException(e);

            try {
                MetricsCollector.getInstance().recordError("csv.import", duration, e.getClass().getSimpleName());
            } catch (IllegalStateException ex) {
                logger.debug("MetricsCollector not initialized, skipping error metrics recording");
            }

            throw e;
        } finally {
            span.end();
            MDC.clear();
        }
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
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("csv.import.by_file")
            .setAttribute("file.path", file != null ? file.getAbsolutePath() : "null")
            .startSpan();

        try {
            MDC.put("operation", "csv.import");
            MDC.put("file.path", file.getAbsolutePath());

            logger.debug("Starting CSV import from file: {}", file.getAbsolutePath());

            Preconditions.checkNotNull(file);

            if(!file.exists()) {
                logger.error("CSV import failed: file not found - {}", file.getName());
                throw new FileNotFoundException("file " + file.getName() + " not found");
            }

            if(!file.isFile()) {
                logger.error("CSV import failed: provided path is not a file - {}", file.getAbsolutePath());
                throw new java.io.IOException("provided file is not a file");
            }

            span.setAttribute("file.size", file.length());
            logger.debug("Processing CSV file of size {} bytes", file.length());

            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            boolean ignoreFirstLine = withHeader;
            int lineCount = 0;

            String strLine;
            while ((strLine = br.readLine()) != null)   {
                lineCount++;
                String[] columns = strLine.split(seperator);
                double[] attributes = new double[columns.length];

                // TODO: adding attributes to data instance
                logger.trace("Processing line {}: {} columns", lineCount, columns.length);
            }

            br.close();
            in.close();
            fstream.close();

            span.setAttribute("lines.processed", lineCount);
            logger.debug("Processed {} lines from CSV file", lineCount);

            try {
                MetricsCollector.getInstance().recordDataProcessed("csv.import", lineCount);
            } catch (IllegalStateException e) {
                logger.debug("MetricsCollector not initialized, skipping data metrics recording");
            }

            throw new UnsupportedOperationException("Not supported yet.");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("CSV import failed after {} ms", duration, e);
            span.recordException(e);

            try {
                MetricsCollector.getInstance().recordError("csv.import", duration, e.getClass().getSimpleName());
            } catch (IllegalStateException ex) {
                logger.debug("MetricsCollector not initialized, skipping error metrics recording");
            }

            throw e;
        } finally {
            span.end();
            MDC.clear();
        }
    }
}
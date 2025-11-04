package de.jmale.core.tools;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import de.jmale.core.data.DataSet;
import de.jmale.core.observability.MetricsCollector;
import de.jmale.core.observability.TracingUtils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        logger.info("Starting CSV import from filename: {}", filename);
        long startTime = System.currentTimeMillis();

        Span span = TracingUtils.spanBuilder("csv_import_by_filename").startSpan();
        try (Scope scope = span.makeCurrent()) {
            if(Strings.isNullOrEmpty(filename)) {
                throw new IllegalArgumentException("filename cannot be empty nor null");
            }

            TracingUtils.addAttribute("filename", filename);
            DataSet result = this.doImport(new File(filename));

            double durationSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            MetricsCollector.getInstance().recordFileImport("csv", durationSeconds, "success");
            logger.info("CSV import completed successfully for file: {}", filename);
            span.setStatus(StatusCode.OK);

            return result;
        } catch (Exception e) {
            double durationSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            MetricsCollector.getInstance().recordFileImport("csv", durationSeconds, "error");
            logger.error("CSV import failed for file: {}", filename, e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
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
        logger.info("Starting CSV import from file: {}", file.getName());
        long startTime = System.currentTimeMillis();

        Span span = TracingUtils.spanBuilder("csv_import_by_file").startSpan();
        try (Scope scope = span.makeCurrent()) {
            Preconditions.checkNotNull(file);

            TracingUtils.addAttribute("file_name", file.getName());
            TracingUtils.addAttribute("file_size", file.length());
            TracingUtils.addAttribute("separator", seperator);
            TracingUtils.addAttribute("with_header", withHeader);

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
            int lineCount = 0;

            String strLine;
            while ((strLine = br.readLine()) != null)   {
                lineCount++;
                String[] columns = strLine.split(seperator);
                double[] attributes = new double[columns.length];

                // TODO: adding attributes to data instance
            }

            TracingUtils.addAttribute("lines_processed", lineCount);
            logger.info("Processed {} lines from CSV file: {}", lineCount, file.getName());

            double durationSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            MetricsCollector.getInstance().recordFileImport("csv", durationSeconds, "success");
            span.setStatus(StatusCode.OK);

            // Close resources
            br.close();
            in.close();
            fstream.close();

            throw new UnsupportedOperationException("Not supported yet.");
        } catch (Exception e) {
            double durationSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            MetricsCollector.getInstance().recordFileImport("csv", durationSeconds, "error");
            logger.error("CSV import failed for file: {}", file.getName(), e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
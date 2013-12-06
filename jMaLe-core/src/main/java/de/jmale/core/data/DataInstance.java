package de.jmale.core.data;

/**
 *
 * @author Christian Vogel (<a href="mailto:contact@christian-voge.info">contact@christian-voge.info</a>)
 * @version 1.0.0 2012-03-30
 * @since 1.0
 */
public interface DataInstance {
    double[] values();
    
    void add(double value);
    
    double value(int index);
    
    int numberOfAttributes();
}

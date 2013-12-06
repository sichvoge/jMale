package de.jmale.core.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Vogel (<a href="mailto:contact@christian-voge.info">contact@christian-voge.info</a>)
 * @version 1.0.0
 * @since 1.0
 */
public class SimpleDataInstance implements DataInstance {
    
    private List<Double> values;
    
    public SimpleDataInstance() {
        values = new ArrayList<Double>();
    }

    public double[] values() {
        if(values.isEmpty()) {
            return new double[0];
        }
        
        double[] arrValues = new double[values.size()];
        int index = 0;
        
        for(Double value : values) {
            arrValues[index++] = value;
        }
        
        return arrValues;
    }

    public void add(double value) {
        values.add(value);
    }

    public double value(int index) {
        if(index > values.size()) {
            throw new IndexOutOfBoundsException();
        }
        
        return values.get(index);
    }

    public int numberOfAttributes() {
        return values.size();
    }

}

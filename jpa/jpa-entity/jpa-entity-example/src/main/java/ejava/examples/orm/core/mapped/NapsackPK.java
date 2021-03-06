package ejava.examples.orm.core.mapped;

import java.io.Serializable;

import javax.persistence.*;

/**
 * This class provides an example of a primary key class that can be 
 * embedded into the referenced class. The containing class will simply
 * use an instance of this class rather than having separate fields that
 * match the fields of this class.
 * 
 * @author jcstaff
 * $Id:$
 */
@Embeddable
public class NapsackPK implements Serializable {
    private static final long serialVersionUID = 1L;
    private String make;
    private String model;
    
    public NapsackPK() {}
    public NapsackPK(String make, String model) {
        this.make = make;
        this.model = model;
    }
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    
    @Column(name="NAPSACK_MAKE") //maps field to column of containing class
    public String getMake() {
        return make;
    }
    public void setMake(String make) {
        this.make = make;
    }

    @Column(name="NAPSACK_MODEL")//maps field to column of containing class
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public int hashCode() {
        return make.hashCode() + model.hashCode();
    }
    public boolean equals(Object obj) {
        try {
            if (this == obj) return true;
            return make.equals(((NapsackPK)obj).getMake()) &&
                   model.equals(((NapsackPK)obj).getModel());
            
        } catch (Throwable ignored) { //catch NP & Cast Exceptions 
            return false;
        }
    }
    
    public String toString() {
        return super.toString() +
            ", make=" + make +
            ", model=" + model;
    }

}

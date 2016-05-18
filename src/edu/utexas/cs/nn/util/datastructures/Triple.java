package edu.utexas.cs.nn.util.datastructures;

/**
 * 3-Tuple of objects
 *
 * @author Jacob Schrum
 */
public class Triple<X, Y, Z> {

    public X t1;
    public Y t2;
    public Z t3;

    public Triple(X pos1, Y pos2, Z pos3) {
        this.t1 = pos1;
        this.t2 = pos2;
        this.t3 = pos3;
    }

    @Override
    public String toString() {
        return "(" + t1.toString() + "," + t2.toString() + "," + t3.toString() + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Triple) {
            Triple p = (Triple) other;
            return t1.equals(p.t1) && t2.equals(p.t2) && t3.equals(p.t3);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.t1 != null ? this.t1.hashCode() : 0);
        hash = 71 * hash + (this.t2 != null ? this.t2.hashCode() : 0);
        hash = 71 * hash + (this.t3 != null ? this.t3.hashCode() : 0);
        return hash;
    }

	public Triple<X,Y,Z> clone() {
		// TODO Auto-generated method stub
		return null;
	}
}

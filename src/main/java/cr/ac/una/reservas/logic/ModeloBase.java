package cr.ac.una.reservas.logic;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
public abstract class ModeloBase {

    private transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(listener);
        }
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }
}
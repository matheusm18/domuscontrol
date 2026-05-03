package domuscontrol.devices;

import domuscontrol.devices.types.OpenableDevice;

/**
 * Represents a smart curtain device.
 */
public class Curtain extends OpenableDevice {

    /** Default constructor for the Curtain class. */
    public Curtain() {
        super();
    }

    /**
     * Constructor for the Curtain class with specified values.
     *
     * @param brand The brand of the curtain.
     * @param model The model of the curtain.
     * @param consumptionPerHour The power consumption in Wh/h.
     * @param openingLevel The initial opening percentage (0-100).
     */
    public Curtain(String brand, String model, double consumptionPerHour, int openingLevel) {
        super(brand, model, consumptionPerHour, openingLevel);
    }

    /**
     * Copy constructor for the Curtain class.
     * 
     * @param curtain The Curtain instance to copy.
     */
    public Curtain(Curtain curtain) {
        super(curtain);
    }

    /** Opens the curtain fully (sets opening to 100%). */
    public void openFully() {
        this.setOpening(100);
    }

    /** Closes the curtain fully (sets opening to 0%). */
    public void closeFully() {
        this.setOpening(0);
    }

    /**
     * Creates a copy of the Curtain instance.
     * 
     * @return A new Curtain instance with the same properties.
     */
    @Override
    public Curtain clone() {
        return new Curtain(this);
    }
}
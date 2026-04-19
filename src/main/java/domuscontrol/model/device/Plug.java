package domuscontrol.model.device;

import domuscontrol.model.device.types.SwitchableDevice;

/**
 * Represents a smart plug device.
 */
public class Plug extends SwitchableDevice {

    /** Default constructor for the Plug class. */
    public Plug() {
        super();
    }

    /**
     * Constructor for the Plug class with specified values.
     * 
     * @param brand The brand of the plug.
     * @param model The model of the plug.
     * @param consumptionPerHour The power consumption rate in Wh/h.
     */
    public Plug(String brand, String model, double consumptionPerHour) {
        super(brand, model, consumptionPerHour);
    }

    /**
     * Copy constructor for the Plug class.
     * 
     * @param plug The Plug instance to copy.
     */
    public Plug(Plug plug) {
        super(plug);
    }
    
    /**
     * Creates a copy of the Plug instance.
     * 
     * @return A new Plug instance with the same properties.
     */
    @Override
    public Plug clone() {
        return new Plug(this);
    }
}
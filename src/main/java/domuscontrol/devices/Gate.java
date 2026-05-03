package domuscontrol.devices;

import domuscontrol.devices.types.OpenableDevice;

/**
 * Represents a smart gate device in the home automation system.
 */
public class Gate extends OpenableDevice {

    /**
     * Default constructor for the Gate class.
     * Initializes a new gate in a fully closed state.
     */
    public Gate() {
        super(); 
    }

    /**
     * Parameterized constructor.
     *
     * @param brand              The brand of the gate.
     * @param model              The model of the gate.
     * @param consumptionPerHour The power consumption of the gate in Wh/h.
     * @param openingLevel       The initial opening percentage (0-100).
     */
    public Gate(String brand, String model, double consumptionPerHour, int openingLevel) {
        super(brand, model, consumptionPerHour, openingLevel); 
    }

    /**
     * Copy constructor.
     *
     * @param gate The Gate object to copy.
     */
    public Gate(Gate gate) {
        super(gate); 
    }

    /**
     * Opens the gate fully (sets opening to 100%).
     */
    public void openFully() {
        this.setOpening(100);
    }

    /**
     * Closes the gate fully (sets opening to 0%).
     */
    public void closeFully() {
        this.setOpening(0);
    }

    /**
     * Creates a copy of this gate.
     *
     * @return A copy of this gate.
     */
    @Override
    public Gate clone() {
        return new Gate(this);
    }
}
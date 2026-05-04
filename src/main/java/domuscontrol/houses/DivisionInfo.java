package domuscontrol.houses;

import java.util.ArrayList;
import java.util.List;

//Auxiliar class for transporting division info.
public class DivisionInfo {
    // The house the division belongs to.
    private final House house;
    // The name of the division.
    private final String divisionName;
    // The list of devices in the division.
    private final List<String> devices;

    /**
     * Constructor for DivisionInfo.
     * @param house The house the division belongs to.
     * @param divisionName The name of the division.
     * @param devices The list of devices in the division.
     */
    public DivisionInfo(House house, String divisionName, List<String> devices) {
        this.house = house != null ? house.clone() : null;
        this.divisionName = divisionName;
        this.devices = devices != null ? new ArrayList<>(devices) : new ArrayList<>();
    }

    /**
     * Gets the house the division belongs to.
     * @return The house the division belongs to.
     */
    public House getHouse() {
        return house != null ? house.clone() : null;
    }

    /**
     * Gets the name of the division.
     * @return The name of the division.
     */
    public String getDivisionName() {
        return divisionName;
    }

    /**
     * Gets the list of devices in the division.
     * @return The list of devices in the division.
     */
    public List<String> getDevices() {
        return new ArrayList<>(devices);
    }
}

package domuscontrol.model.houses;

import java.util.List;

//Auxiliar class for transporting division info.
public class DivisionInfo {
    // The house the division belongs to.
    public final House house;
    // The name of the division.
    public final String divisionName;
    // The list of devices in the division.
    public final List<String> devices;
    /**
     * Constructor for DivisionInfo.
     * @param house The house the division belongs to.
     * @param divisionName The name of the division.
     * @param devices The list of devices in the division.
     */
    public DivisionInfo(House house, String divisionName, List<String> devices) {
        this.house = house;
        this.divisionName = divisionName;
        this.devices = devices;
    }
}
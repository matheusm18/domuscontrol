package domuscontrol;

import domuscontrol.view.DomusControlTextUI;

public class DomusControlApplication {

    private DomusControlController controller;

    public void init() {
        this.controller = new DomusControlController();
    }

    public void start() {
        new DomusControlTextUI(this.controller).run();
    }
}
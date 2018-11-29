package com.team_olora.olora_beta;

import com.squareup.otto.Bus;

public class Provider_BlueOn {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private Provider_BlueOn() {

    }
}

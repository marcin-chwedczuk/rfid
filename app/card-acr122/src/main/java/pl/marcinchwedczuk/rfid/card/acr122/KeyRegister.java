package pl.marcinchwedczuk.rfid.card.acr122;

public enum KeyRegister {
    REGISTER_0 {
        @Override
        int index() {
            return 0;
        }
    },
    REGISTER_1 {
        @Override
        int index() {
            return 1;
        }
    };

    abstract int index();
}

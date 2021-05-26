package pl.marcinchwedczuk.rfid.card.acr122;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class Sector {
    public final int index;

    private Sector(int index) {
        if (index < 0) throw new IllegalArgumentException("index");

        if (index > 0x40) throw new IllegalArgumentException(
                "Mifare card can have at most 0x40 sectors (Mifare 4K).");

        this.index = index;
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }

    public static Sector of(int number) {
        return new Sector(number);
    }

    public static List<Sector> range(int fromSector, int toSectorExcluding) {
        return IntStream
                .range(fromSector, toSectorExcluding)
                .mapToObj(Sector::of)
                .collect(toList());
    }
}

package noedit;

public final class Data {

    private final int nr;

    public Data(int nr) {
        this.nr = nr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return nr == data.nr;
    }

    @Override
    public int hashCode() {
        return nr;
    }
}

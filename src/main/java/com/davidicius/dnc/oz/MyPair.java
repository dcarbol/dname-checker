package com.davidicius.dnc.oz;

public class MyPair<A, B> {
    private final A a;
    private final B b;

    public MyPair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyPair)) return false;

        MyPair myPair = (MyPair) o;

        return a.equals(myPair.a) && b.equals(myPair.b);

    }

    @Override
    public int hashCode() {
        int result = a.hashCode();
        result = 31 * result + b.hashCode();
        return result;
    }
}

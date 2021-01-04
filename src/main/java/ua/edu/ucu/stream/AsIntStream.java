package ua.edu.ucu.stream;

import ua.edu.ucu.function.*;

import java.util.ArrayList;
import java.util.Iterator;

public class AsIntStream implements IntStream {
    private final ArrayList<Integer> innerArr;
    private boolean terminated;

    private AsIntStream(int[] values) {
        this.innerArr = new ArrayList<>();
        terminated = false;
        for (int x: values){
            this.innerArr.add(x);
        }
    }

    private AsIntStream(Iterator<Integer> it) {
        this.innerArr = new ArrayList<>();
        terminated = false;
        while (it.hasNext()){
            this.innerArr.add(it.next());
        }
    }

    public static IntStream of(int... values) {
        return new AsIntStream(values);
    }

    @Override
    public Double average() {
        if (terminated){
            throw new IllegalArgumentException("Empty");
        }
        int len = 0;
        double sum = 0;
        Iterator<Integer> iter = this.innerArr.iterator();
        while (iter.hasNext()){
            int x = iter.next();
            sum += x;
            len++;
        }
        terminated = true;
        return (sum / len);

    }

    @Override
    public Integer max() {
        return reduce(Integer.MIN_VALUE, Math::max);
    }

    @Override
    public Integer min() {
        return reduce(Integer.MAX_VALUE, Math::min);
    }

    @Override
    public long count() throws IllegalArgumentException{
        return reduce(0, (x, y) -> {return x + 1;});
    }

    @Override
    public Integer sum() throws IllegalArgumentException{
        return reduce(0, Integer::sum);
    }

    @Override
    public IntStream filter(IntPredicate predicate) {
        Iterator<Integer> iter = this.innerArr.iterator();
        Iterator<Integer> resultIter = new Iterator<Integer>() {
            private boolean checked = false;
            private int x;
            @Override
            public boolean hasNext() {
                if (checked){
                    return true;
                }
                while (iter.hasNext()){
                    int val = iter.next();
                    if (predicate.test(val)){
                        x = val;
                        checked = true;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Integer next() {
                if (hasNext()){
                    checked = false;
                    return x;
                }
                return null;
            }
        };
        return new AsIntStream(resultIter);
    }

    @Override
    public void forEach(IntConsumer action) {
        if (terminated){
            throw new IllegalArgumentException("Empty");
        }
        Iterator<Integer> iter = this.innerArr.iterator();
        while (iter.hasNext()){
            action.accept(iter.next());
        }
        terminated = true;
    }

    @Override
    public IntStream map(IntUnaryOperator mapper) {
        Iterator<Integer> iter = this.innerArr.iterator();
        Iterator<Integer> resultIter = new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Integer next() {
                return mapper.apply(iter.next());
            }
        };
        return new AsIntStream(resultIter);
    }

    @Override
    public IntStream flatMap(IntToIntStreamFunction func) {
        Iterator<Integer> iter = this.innerArr.iterator();
        Iterator<Integer> resultIter = new Iterator<Integer>() {
            Iterator<Integer> curr = ((AsIntStream) func.applyAsIntStream(iter.next())).innerArr.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext() || curr.hasNext();
            }

            @Override
            public Integer next() {
                if (hasNext()){
                    if (curr.hasNext()){
                        return curr.next();
                    }
                    curr = ((AsIntStream) func.applyAsIntStream(iter.next())).innerArr.iterator();
                    return next();
                }
                return null;
            }
        };
        return new AsIntStream(resultIter);
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) throws IllegalArgumentException {
        if (terminated){
            throw new IllegalArgumentException("Empty");
        }
        Iterator<Integer> iter = this.innerArr.iterator();
        while (iter.hasNext()){
            int x = iter.next();
            identity = op.apply(identity, x);
        }
        terminated = true;
        return identity;
    }

    @Override
    public int[] toArray() throws IllegalArgumentException {
        if (terminated){
            throw new IllegalArgumentException("Empty");
        }
        int[] result = new int[this.innerArr.size()];
        Iterator<Integer> iter = this.innerArr.iterator();
        int idx = 0;
        while (iter.hasNext()){
            int x = iter.next();
            result[idx] = x;
            idx++;
        }
        terminated = true;
        return result;
    }

}

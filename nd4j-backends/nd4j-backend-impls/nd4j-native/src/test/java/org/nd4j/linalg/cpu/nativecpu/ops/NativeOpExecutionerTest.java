package org.nd4j.linalg.cpu.nativecpu.ops;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.Exp;
import org.nd4j.linalg.api.ops.impl.transforms.SoftMax;
import org.nd4j.linalg.api.ops.impl.transforms.SoftMaxDerivative;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * @author raver119@gmail.com
 */
@Ignore
public class NativeOpExecutionerTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void execBroadcastOp() throws Exception {
        INDArray array = Nd4j.ones(1024, 1024);
        INDArray arrayRow = Nd4j.linspace(1, 1024, 1024);

        float sum = (float) array.sumNumber().doubleValue();

        array.addiRowVector(arrayRow);

        long time1 = System.nanoTime();
        for (int x = 0; x < 1000; x++) {
            array.addiRowVector(arrayRow);
        }
        long time2 = System.nanoTime();

        System.out.println("Execution time: " + ((time2 - time1) / 1000));

        assertEquals(1002, array.getFloat(0), 0.1f);
        assertEquals(2003, array.getFloat(1), 0.1f);
    }

    @Test
    public void execReduceOp1() throws Exception {
        INDArray array = Nd4j.ones(1024, 1024);
        INDArray arrayRow1 = Nd4j.linspace(1, 1024, 1024);
        INDArray arrayRow2 = Nd4j.linspace(0, 1023, 1024);

        float sum = (float) array.sumNumber().doubleValue();
        long time1 = System.nanoTime();
        array.sum(0);
        long time2 = System.nanoTime();

        System.out.println("Execution time: " + (time2 - time1));
        System.out.println("Result: " + sum);
    }

    @Test
    public void execReduceOp2() throws Exception {
        INDArray array = Nd4j.ones(3, 1024);
        INDArray arrayRow = Nd4j.linspace(1, 1024, 1024);

        float sum = array.sumNumber().floatValue();
        long time1 = System.nanoTime();
        sum = array.sumNumber().floatValue();
        long time2 = System.nanoTime();

        System.out.println("Execution time: " + (time2 - time1));
    }

    @Test
    public void execTransformOp1() throws Exception {
        INDArray array1 = Nd4j.linspace(1, 20480, 20480);
        INDArray array2 = Nd4j.linspace(1, 20480, 20480);

        Nd4j.getExecutioner().exec(new Exp(array1, array2));

        long time1 = System.nanoTime();
        for (int x = 0; x < 10000; x++) {
            Nd4j.getExecutioner().exec(new Exp(array1, array2));
        }
        long time2 = System.nanoTime();

        System.out.println("Execution time: " + ((time2 - time1) / 10000));

       // System.out.println("Array1: " + array1);
       // System.out.println("Array2: " + array2);

        assertEquals(2.71f, array2.getFloat(0), 0.01);
    }

    @Test
    public void execPairwiseOp1() throws Exception {
        INDArray array1 = Nd4j.linspace(1, 20480, 20480);
        INDArray array2 = Nd4j.linspace(1, 20480, 20480);

        array1.sumNumber();
        array2.sumNumber();

        long time1 = System.nanoTime();
        for (int x = 0; x < 10000; x++) {
            array1.addiRowVector(array2);
        }
        long time2 = System.nanoTime();

        System.out.println("Execution time: " + ((time2 - time1) / 10000));

        // System.out.println("Array1: " + array1);
        // System.out.println("Array2: " + array2);

        assertEquals(10001f, array1.getFloat(0), 0.01);
    }


    @Test
    public void testScalarOp1() throws Exception {
        // simple way to stop test if we're not on CUDA backend here

        INDArray array1 = Nd4j.linspace(1, 20480, 20480);
        INDArray array2 = Nd4j.linspace(1, 20480, 20480);
        array2.addi(0.5f);

        long time1 = System.nanoTime();
        for (int x = 0; x < 10000; x++) {
            array2.addi(0.5f);
        }
        long time2 = System.nanoTime();

        System.out.println("Execution time: " + ((time2 - time1) / 10000));

        System.out.println("Divi result: " + array2.getFloat(0));
        assertEquals(5001.5, array2.getFloat(0), 0.01f);

    }

    @Test
    public void testSoftmax1D_1() throws Exception {
        INDArray input1T = Nd4j.create(new double[]{ -0.75, 0.58, 0.42, 1.03, -0.61, 0.19, -0.37, -0.40, -1.42, -0.04}).transpose();
        INDArray input1 = Nd4j.create(new double[]{ -0.75, 0.58, 0.42, 1.03, -0.61, 0.19, -0.37, -0.40, -1.42, -0.04});
        INDArray input2 = Nd4j.zerosLike(input1);
        Nd4j.copy(input1, input2);
        INDArray output1 = Nd4j.create(1, 10);
        INDArray output1T = Nd4j.create(1, 10);

        System.out.println("FA --------------------");
        Nd4j.getExecutioner().exec(new SoftMax(input1, output1));
        Nd4j.getExecutioner().exec(new SoftMax(input1T, output1T));
        System.out.println("FB --------------------");

        System.out.println("Softmax = " + output1);
        INDArray output2 = Nd4j.create(1,10);
        Nd4j.getExecutioner().exec(new SoftMaxDerivative(input2, output2));
        System.out.println("Softmax Derivative = " + output2);

        INDArray assertion1 = Nd4j.create(new double[]{0.04, 0.16, 0.14, 0.26, 0.05, 0.11, 0.06, 0.06, 0.02, 0.09});

        assertArrayEquals(assertion1.data().asFloat(), output1.data().asFloat(), 0.01f);
        assertArrayEquals(assertion1.data().asFloat(), output1T.data().asFloat(), 0.01f);

    }
}
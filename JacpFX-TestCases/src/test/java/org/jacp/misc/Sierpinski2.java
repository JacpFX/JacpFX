/*
 * **********************************************************************
 *
 *  Copyright (C) 2010 - 2015
 *
 *  [Component.java]
 *  JACPFX Project (https://github.com/JacpFX/JacpFX/)
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 * *********************************************************************
 */

package org.jacp.misc;

/**
 * Created by Andy Moncsek on 14.04.15.
 */
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Sierpinski2 extends Application
{
    static boolean USE_RECTS;
    static boolean USE_PATH;
    static boolean verbose;

    class Triangle
    {
        private final double topX;
        private final double topY;
        private final double height;

        public Triangle(double topX, double topY, double height)
        {
            this.topX = topX;
            this.topY = topY;
            this.height = height;
        }

        public final double getTopX()
        {
            return topX;
        }

        public final double getTopY()
        {
            return topY;
        }

        public final double getHeight()
        {
            return height;
        }
    }

    private final double width = 800;
    private final double height = 600;
    private final double smallest = 8;

    private GraphicsContext gc;
    private List<Triangle> renderList;

    private final double[] pointsX = new double[3];
    private final double[] pointsY = new double[3];

    private double rootHeight;


    private final void calcTriangles()
    {
        renderList.clear();

        double acceleration = rootHeight * 0.02;

        rootHeight += acceleration;

        if (rootHeight >= 2 * height)
        {
            rootHeight = height;
        }

        Triangle root = new Triangle(width / 2, 0, rootHeight);

        shrink(root);
    }

    private void shrink(Triangle tri)
    {
        double topX = tri.getTopX();
        double topY = tri.getTopY();
        double triangleHeight = tri.getHeight();

        if (topY >= height)
        {
            return;
        }

        if (triangleHeight < smallest)
        {
            renderList.add(tri);
        }
        else
        {
            Triangle top = new Triangle(topX, topY, triangleHeight / 2);
            Triangle left = new Triangle(topX - triangleHeight / 4, topY + triangleHeight / 2, triangleHeight / 2);
            Triangle right = new Triangle(topX + triangleHeight / 4, topY + triangleHeight / 2, triangleHeight / 2);

            shrink(top);
            shrink(left);
            shrink(right);
        }
    }

    private final void clearBackground()
    {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);
    }

    private final void drawTriangles()
    {
        gc.setFill(Color.WHITE);
        gc.beginPath();

        int triangleCount = renderList.size();
        if (verbose) {
            System.out.println(triangleCount+" triangles");
        }

        for (int i = 0; i < triangleCount; i++)
        {
            Triangle tri = renderList.get(i);

            if (tri.getTopY() < height)
            {
                drawTriangle(tri);
            }
        }
        if (USE_PATH) {
            gc.fill();
        }
    }

    private final void drawTriangle(Triangle tri)
    {
        double topX = tri.getTopX();
        double topY = tri.getTopY();
        double h = tri.getHeight();

        if (USE_RECTS) {
            gc.fillRect(topX - h/2, topY, h, h);
        } else {
            pointsX[0] = topX;
            pointsY[0] = topY;

            pointsX[1] = topX + h / 2;
            pointsY[1] = topY + h;

            pointsX[2] = topX - h / 2;
            pointsY[2] = topY + h;

            if (USE_PATH) {
                gc.moveTo(pointsX[0], pointsY[0]);
                gc.lineTo(pointsX[1], pointsY[1]);
                gc.lineTo(pointsX[2], pointsY[2]);
            } else {
                gc.fillPolygon(pointsX, pointsY, 3);
            }
        }
    }

    public static void main(String[] args)
    {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("rects")) {
                USE_RECTS = true;
            } else if (arg.equalsIgnoreCase("path")) {
                USE_PATH = true;
            } else if (arg.equalsIgnoreCase("verbose")) {
                verbose = true;
            } else {
                USE_PATH = true;
                //System.err.println("Unrecognized argument: "+arg);
                //System.exit(-1);
            }
        }

        USE_RECTS = true;
        Application.launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception
    {
        BorderPane root = new BorderPane();

        Scene scene;

        scene = new Scene(root, width, height);

        renderList = new ArrayList<>();
        rootHeight = height;

        Canvas canvas = new Canvas(width, height);

        gc = canvas.getGraphicsContext2D();

        root.setCenter(canvas);

        stage.setTitle("GraphicsContext.fillPolygon performance test");
        stage.setScene(scene);
        stage.show();

        AnimationTimer timer = new AnimationTimer()
        {
            private long nextSecond = 0;
            private int framesPerSecond = 0;

            @Override
            public void handle(long startNanos)
            {
                calcTriangles();

                clearBackground();

                drawTriangles();

                framesPerSecond++;

                if (startNanos > nextSecond)
                {
                    System.out.println("fps: " + framesPerSecond);
                    framesPerSecond = 0;
                    nextSecond = startNanos + 1_000_000_000L;
                }
            }
        };

        timer.start();
    }
}
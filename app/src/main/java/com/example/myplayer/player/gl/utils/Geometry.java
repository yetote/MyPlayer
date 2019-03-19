package com.example.myplayer.player.gl.utils;


public class Geometry {
    public static Vector vectorBetween(Point from, Point to) {
        return new Vector(
                to.x - from.x,
                to.y - from.y,
                to.z - from.z);
    }

    public static boolean intersects(Sphere sphere, Ray ray) {
        return distanceBetween(sphere.center, ray) < sphere.radius;
    }

    private static float distanceBetween(Point point, Ray ray) {
        Vector p1ToPoint = vectorBetween(ray.point, point);
        Vector p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point);

        float areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length();
        float lengthOfBase = ray.vector.length();

        float distanceFromPointToRay = areaOfTriangleTimesTwo / lengthOfBase;
        return distanceFromPointToRay;

    }

    public static Point intersectionPoint(Ray ray, Plane plane) {
        Vector rayToPlaneVector = vectorBetween(ray.point, plane.point);
        float scaleFactor = rayToPlaneVector.dotProduct(plane.normal)
                / ray.vector.dotProduct(plane.normal);
        Point intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor));
        return intersectionPoint;
    }

    /**
     * 点
     */
    public static class Point {
        public final float x, y, z;

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * 沿Y轴平移
         *
         * @param distance 平移的距离
         * @return 平移后的点的位置
         */
        public Point translateY(float distance) {
            return new Point(x, y + distance, z);
        }

        public Point translate(Vector vector) {
            return new Point(
                    x + vector.x,
                    y + vector.y,
                    z + vector.z);
        }
    }

    /**
     * 圆
     */
    public static class Circle {
        public final Point center;
        public final float radius;


        public Circle(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }

        /**
         * 圆的缩放
         *
         * @param scale 缩放的大小（和半径的比例）
         * @return 经过缩放产生的新的圆
         */
        public Circle scale(float scale) {
            return new Circle(center, radius * scale);
        }
    }

    /**
     * 圆柱
     */
    public static class Cylinder {
        public final Point center;
        public final float radius;
        public final float height;

        public Cylinder(Point center, float radius, float height) {
            this.center = center;
            this.radius = radius;
            this.height = height;
        }
    }

    public static class Ray {
        private final Point point;
        private final Vector vector;

        public Ray(Point point, Vector vector) {
            this.point = point;
            this.vector = vector;
        }
    }

    public static class Vector {
        public final float x;
        public final float y;
        public final float z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length() {
            return (float) Math.sqrt(
                            x * x +
                            y * y +
                            z * z);
        }

        public Vector crossProduct(Vector other) {
            return new Vector(
                    (y * other.z) - (z * other.y),
                    (z * other.x) - (x * other.z),
                    (x * other.y) - (y * other.x)
            );
        }

        public float dotProduct(Vector other) {

            return x * other.x +
                    y * other.y +
                    z * other.z;
        }

        public Vector scale(float f) {

            return new Vector(
                    x * f,
                    y * f,
                    z * f);
        }
    }

    public static class Sphere {
        private final Point center;
        private final float radius;

        public Sphere(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }
    }

    public static class Plane {
        public final Point point;
        public final Vector normal;

        public Plane(Point point, Vector normal) {
            this.point = point;
            this.normal = normal;
        }
    }
}

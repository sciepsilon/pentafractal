import java.util.ArrayList;
import java.util.List;

public class Main{
  
  public static abstract class Shape {
    
    public String toLatex() {
      // by default, draw an outline of the shape
      return this.toLatex("[draw]");
    }
    
    abstract String toLatex(String options);
    
  }
  
  public static class RegularPentagon extends Shape {
  
    double xCenter;
    double yCenter;
    double r;
    double theta; // angle of vertex0 above the horizontal
    double[] xs;
    double[] ys;
    
    public RegularPentagon(double xCenter, double yCenter, double x1, double y1) {
      this.xCenter = xCenter;
      this.yCenter = yCenter;
      this.r = Math.hypot(xCenter - x1, yCenter - y1);
      if (x1 > xCenter) {
        this.theta = Math.atan((yCenter - y1) / (xCenter - x1));
      } else if (x1 < xCenter) {
        this.theta = Math.PI + Math.atan((yCenter - y1) / (xCenter - x1));
      } else if (y1 > yCenter) {
        this.theta = Math.PI / 2;
      } else {
        this.theta = Math.PI / -2;
      }
      
      this.xs = new double[5];
      this.ys = new double[5];
      for (int v=0; v<5; v++) {
        xs[v] = this.getX(this.getAngle(v));
        ys[v] = this.getY(this.getAngle(v));
      }
    }
    
    private double getX(double theta) {
      return xCenter + r * Math.cos(theta);
    }
    
    private double getY(double theta) {
      return yCenter + r * Math.sin(theta);
    }
    
    private double getAngle(int vertexNum) {
      return theta + (Math.PI * 2 * vertexNum / 5);
    }
    
    public RegularPentagon getInnerPentagon() {
      // vertex 0 of the inner pentagon is between vertices 0 and 1 of this pentagon
      
      /* for a pentagram (5-pointed star) 
      where the inner pentagon's side has length 1,
      the circumscribed pentagon's side has length phi squared. 
      */
      double phi = (1 + Math.sqrt(5)) / 2;
      double innerR = this.r / phi / phi;
      double innerTheta = theta + (Math.PI / 5);
      return new RegularPentagon(
        xCenter, 
        yCenter, 
        xCenter + innerR * Math.cos(innerTheta), 
        yCenter + innerR * Math.sin(innerTheta)
      );
    }
    
    // List the triangles outside an inscribed pentagram. 
    public List<Triangle> getOuterTriangles() {
      RegularPentagon ip = this.getInnerPentagon();
      ArrayList<Triangle> result = new ArrayList();
      for (int i=0; i<5; i++) {
        result.add(new Triangle(
          xs[i], 
          ys[i], 
          ip.xs[i], 
          ip.ys[i], 
          xs[(i+1)%5], 
          ys[(i+1)%5]
        ));
      }
      
      return result;
    }
    
    // List the arms of an inscribed pentagram.
    public List<Triangle> getInnerTriangles() {
      RegularPentagon ip = this.getInnerPentagon();
      ArrayList<Triangle> result = new ArrayList();
      for (int i=0; i<5; i++) {
        result.add(new Triangle(
          ip.xs[(i+4)%5], 
          ip.ys[(i+4)%5],
          xs[i], 
          ys[i], 
          ip.xs[i], 
          ip.ys[i]
        ));
      }
      
      return result;
    }
    
    public List<Triangle> getTriangles() {
      List<Triangle> triangles = this.getOuterTriangles();
      triangles.addAll(this.getInnerTriangles());
      return triangles;
    }

    public String toLatex(String options) {
      
      String out = String.format("\\path%s ", options);
      for (int v=0; v<5; v++) {
        out += String.format("(%f, %f) -- ", this.xs[v], this.ys[v]);
      }
      out += "cycle;";
      
      return out;
    }
  }
  
  public static class Triangle extends Shape {
  
    double x1;
    double y1;
    double x2;
    double y2;
    double x3;
    double y3;

    public Triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.x3 = x3;
        this.y3 = y3;
    }

    public List<Triangle> getFragments() {
      double xMid1 = (x2 + x3) / 2;
      double yMid1 = (y2 + y3) / 2;
      double xMid2 = (x1 + x3) / 2;
      double yMid2 = (y1 + y3) / 2;
      double xMid3 = (x1 + x2) / 2;
      double yMid3 = (y1 + y2) / 2;

      // centroid is 2/3 of the way from any vertex to opposite midpoint
      double xCentroid = (x1 + 2 * xMid1) / 3;
      double yCentroid = (y1 + 2 * yMid1) / 3;
      
      List<Triangle> out = new ArrayList();
      
      out.add(new Triangle(x1, y1, xMid2, yMid2, xCentroid, yCentroid));
      out.add(new Triangle(x1, y1, xMid3, yMid3, xCentroid, yCentroid));
      out.add(new Triangle(x2, y2, xMid1, yMid1, xCentroid, yCentroid));
      out.add(new Triangle(x2, y2, xMid3, yMid3, xCentroid, yCentroid));
      out.add(new Triangle(x3, y3, xMid1, yMid1, xCentroid, yCentroid));
      out.add(new Triangle(x3, y3, xMid2, yMid2, xCentroid, yCentroid));

      return out;
    }
    
    public String toLatex(String options) {
      return String.format("\\path%s (%f, %f) -- (%f, %f) -- (%f, %f) -- cycle;", 
          options, x1, y1, x2, y2, x3, y3);
    }
  }
  
  public static void testSingleLevel() {
    Triangle tri1 = new Triangle(0,0,0,1,1,0);
    System.out.println(tri1.toLatex());
    List<Triangle> fragments = tri1.getFragments();
    for (int i=0; i<fragments.size(); i++) {
        System.out.println(fragments.get(i).toLatex());
    }
    
    // vertex points up
    RegularPentagon pentUp = new RegularPentagon(0, -3, 0, -2);
    System.out.println(pentUp.toLatex());
    
    // vertex points right
    RegularPentagon pentRight = new RegularPentagon(0, -5, 1, -5);
    System.out.println(pentRight.toLatex());
    
    // vertex points up and to the right
    RegularPentagon pentUpRight = new RegularPentagon(0, -7, 1, -6);
    System.out.println(pentUpRight.toLatex());
    
    // vertex points down
    RegularPentagon pentDown = new RegularPentagon(0, -9, 0, -10);
    System.out.println(pentDown.toLatex());
    
    // vertex points left
    RegularPentagon pentLeft = new RegularPentagon(0, -11, -1, -11);
    System.out.println(pentLeft.toLatex());
    
    // vertex points up and to the left
    RegularPentagon pentUpLeft = new RegularPentagon(0, -13, -1, -12);
    System.out.println(pentUpLeft.toLatex());
    
    // vertex points down and to the left
    RegularPentagon pentDownLeft = new RegularPentagon(0, -15, -1, -16);
    System.out.println(pentDownLeft.toLatex());
    
    RegularPentagon degeneratePent = new RegularPentagon(4,-4,4,-4);
    System.out.println(degeneratePent.toLatex());
    
    RegularPentagon innerPentLeft = pentLeft.getInnerPentagon();
    System.out.println(innerPentLeft.toLatex());
    
    List<Triangle> pentFragments = pentUpLeft.getTriangles();
    for (int i=0; i<pentFragments.size(); i++) {
        System.out.println(pentFragments.get(i).toLatex());
    }
  }
  
  public static List<Shape> pentafractal(double xCenter, double yCenter, double x1, double y1, int numIterations) {
    RegularPentagon pentagon = new RegularPentagon(xCenter, yCenter, x1, y1);
    List<Shape> triangles = new ArrayList();
    for (int i=0; i<numIterations; i++) {
      List<Shape> oldTriangles = triangles;
      triangles = new ArrayList();
      for (int j=0; j<oldTriangles.size(); j++) {
        triangles.addAll(((Triangle) oldTriangles.get(j)).getFragments());
      }
      triangles.addAll(((RegularPentagon) pentagon).getTriangles());
      pentagon = pentagon.getInnerPentagon();
    }
    ((List<Shape>) triangles).add(pentagon);
    
    return triangles;
  }
  
  public static void main(String[] args) {
    
    // prevent "fringes" from showing up oustide corners of fractal
    double outerLineWidth = 0.2; // cm
    double innerLineWidth = 0.03;
    System.out.println((new RegularPentagon(0,0,0,9 + (outerLineWidth / 2))).toLatex("[clip]"));
    
    List<Shape> shapes = pentafractal(0,0,0,9,4);
    for (int i=0; i<shapes.size(); i++) {
      System.out.println("\\randomcolor" + shapes.get(i).toLatex("[draw,line width=" + Double.toString(innerLineWidth) + "cm,fill=randomcolor!80!magenta]"));
    }
    System.out.println();
    List<Shape> shapes2 = pentafractal(0,0,0,9,2);
    for (int i=0; i<shapes2.size(); i++) {
      System.out.println(shapes2.get(i).toLatex("[draw,line width=" + Double.toString(outerLineWidth) + "cm]"));
    }
    
  }
  
}
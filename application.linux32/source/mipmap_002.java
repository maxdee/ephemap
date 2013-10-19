import processing.core.*; 
import processing.data.*; 
import processing.opengl.*; 

import oscP5.*; 
import netP5.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class mipmap_002 extends PApplet {




/*
mipmap, rapid ephemeral mapper
 Version 002 implements OSC
 */




//step mode with9 external inconsistent slow clock

//arrow keys and mouse to map.

ArrayList<PVector> points;
float d = 0.1f;
float speed = 0.01f;
int pointSize = 5;
int lineSize = 2;
int col;
int mode = 1;
boolean trails = true;
// 0 blackout, 1 edit, 2 rainbow kittens.

int px = 0;
int py = 0;

int x1 = 0;
int x2 = 0;
int y1 = 0;
int y2 = 0;

int pcol = color(0, 0, 0);
int blocIndex = 0;
int[] cols = new int[100];

boolean mouse = true;


int shift = 0;


boolean oscEn = true;
int porter = 5656;
OscP5 oscp;


public void setup() {
  size(1290, 810, P3D);
  points = new ArrayList();
  smooth();
  noCursor();
  colorMode(HSB, 100);
  background(0);
  placePoint();

  if (oscEn) {
    oscp = new OscP5(this, porter);
  }

  //random colour pallett
  for (int i = 0; i<cols.length; i++) {
    cols[i] = PApplet.parseInt(random(100));
  }
}

/// does this even work?
public boolean skecthFullScreen() {
  return false;
}


public void draw() {
  step();
  bg(); 
  if (points.size()<2) maper();
  else {
    for (int i = points.size()-1; i > 1; i--) {
      int j = i - 1;
      if (points.get(i).z!=255) {
        x1 = PApplet.parseInt(points.get(j).x);
        y1 = PApplet.parseInt(points.get(j).y);
        x2 = PApplet.parseInt(points.get(i).x);
        y2 = PApplet.parseInt(points.get(i).y);

        switch(mode) {
        case 0:
          background(0);
          break;
        case 1:
          maper();
          break;
        case 2:
          polka(i);
          break;
        case 3:
          dotted(i);
          break;
        }
      }
    }
  }
}





public void maper() {
  trails = false;
  placePoint();
  drawLines();
  removePoint();

  if (!oscEn && mouse) {
    px=mouseX;
    py=mouseY;
  }
  aimer(px, py);
}

////// STUFF THAT MAKES PIXELS CHANGE COLOR  \\\\\\\\\\\\

public void polka(int i) {
  stroke(getCol(i));
  strokeWeight(pointSize);
  point(x1+d*(x2-x1), y1+d*(y2-y1));
}


public void dotted(int i) {
  int n = 24;
  //adjust number of dots:
  int l = PApplet.parseInt(sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2)));
  n=l/30;  
  stroke(getCol(i));
  strokeWeight(pointSize);
  for (float k = 0; k<n;k++) {   
    float e = (d+k)/n;     
    point(x1+e*(x2-x1), y1+e*(y2-y1));
  }
}


public void drawLines() {
  background(0);
  if (points.size()>2) {
    for (int i = points.size()-1; i > 1; i--) { 
      int j = i - 1;
      int x1 = PApplet.parseInt(points.get(j).x);
      int y1 = PApplet.parseInt(points.get(j).y);
      int x2 = PApplet.parseInt(points.get(i).x);
      int y2 = PApplet.parseInt(points.get(i).y);
      stroke(getCol(i));
      strokeWeight(lineSize);
      line(x1, y1, x2, y2);
      if (d > 1) d=0;
      strokeWeight(pointSize);
      point(x1+d*(x2-x1), y1+d*(y2-y1));
    }
  }
}



public void aimer(int xx, int yy) {
  strokeWeight(3);
  stroke(22, 100, 100);
  int out = 20;
  int in = 3;
  if (mouse) {
    line(xx-out, yy-out, xx-in, yy-in);
    line(xx+out, yy+out, xx+in, yy+in);
    line(xx+out, yy-out, xx+in, yy-in);
    line(xx-out, yy+out, xx-in, yy+in);
  }
  else {
    line(xx-out, yy, xx-in, yy);
    line(xx+out, yy, xx+in, yy);
    line(xx, yy-out, xx, yy-in);
    line(xx, yy+out, xx, yy+in);
  }
}


//////SMALL FUNCTIONS\\\\\\\

public int getCol(int i) {
  if (points.get(i).z == 255) return color(0);
  else return color(cols[PApplet.parseInt(points.get(i).z)], 100, 100);
}

public void step() {
  d = d+speed;
  if (d>1) d=0;
  if (d<0) d=1;
}

public void bg() {
  if (trails) {
    fill(0, 0, 0, 10);
    rect(-5, -5, 5+width, 5+height);
  }
  else {
    background(0);
  }
}

public void placePoint() {
  points.add(new PVector(px, py, blocIndex));
}

public void blackPoint() {
  //removePoint();
  points.add(new PVector(px, py, 255));
  blocIndex++;
}

public void removePoint() {
  if (points.get(points.size()-1).z == 255) blocIndex--;
  if (points.size()>0) points.remove(points.size()-1);
}


/////////////////////  INPUT  \\\\\\\\\\\\\\\\\\\\\\\\\

public void mousePressed() {
  if (mode==1&&mouse) {
    if (mouseButton == LEFT) {
      placePoint();
    } 
    else if (mouseButton == RIGHT) {
      removePoint();
    }
  }
}


public void keyReleased() {
  if (keyCode == SHIFT) {
    shift = 0;
  }
}








////////////// OSC   \\\\\\\\\\\\\\\\


public void oscEvent(OscMessage mess) {
  println(mess);
  if (mess.checkAddrPattern("/mipmap/map/xy")==true) {
    px = mess.get(0).intValue();
    py = mess.get(1).intValue();
  }

  if (mess.checkAddrPattern("/mipmap/map/act")==true) {
    switch(mess.get(0).intValue()) {
    case 1:
      placePoint();
      break;
    case 2:
      removePoint();
      break;
    case 3:
      blackPoint();
      break;
    case 4:
      py-=(1+shift);
      if (py<0) py=height;
      break;
    case 5:
      py+=(1+shift);
      py=py%width;
      break;
    case 6:
      px-=(1+shift);
      if (px<0) px=width;
      break;    
    case 7:
      px+=(1+shift);
      px=px%height;
      break;
    }
  }

  if (mess.checkAddrPattern("/mipmap/ctl/mode")==true) {
    mode = mess.get(0).intValue();
  }

  if (mess.checkAddrPattern("/mipmap/ctl/pointSize")==true) {
    pointSize = mess.get(0).intValue();
  }  

  if (mess.checkAddrPattern("/mipmap/ctl/lineSize")==true) {
    lineSize = mess.get(0).intValue();
  }  

  if (mess.checkAddrPattern("/mipmap/ctl/cols")==true) {
    cols[mess.get(0).intValue()] = mess.get(1).intValue();
  }  

  if (mess.checkAddrPattern("/mipmap/ctl/trails")==true) {
    trails = PApplet.parseBoolean(mess.get(0).intValue());
  }  

  if (mess.checkAddrPattern("/mipmap/ctl/speed")==true) {
    speed = mess.get(0).floatValue();
  }
}




public void keyPressed() {
  if (!oscEn) {
    if (key == CODED) {
      if (keyCode == SHIFT) {
        shift = 10;
      }
      else if (keyCode == LEFT) {
        px-=(1+shift);
        if (px<0) px=width;
      } 
      else if (keyCode == RIGHT) {
        px+=(1+shift);
        px=px%height;
      } 
      else if (keyCode == UP) {
        py-=(1+shift);
        if (py<0) py=height;
      } 
      else if (keyCode == DOWN) {
        py+=(1+shift);
        py=py%width;
      }
    }
    else if (key == 32) {
      placePoint();
    }
    else if (key == 'b') {
      blackPoint();
    }
    else if (key == 'm') {
      mouse=!mouse;
      println("Mouse = "+mouse);
    }
    else if (key == 'r') {
      speed = speed * -1;
    }
    else if (key == 't') {
      trails=!trails;
      println("Trails = "+trails);
    }
    else if (key == 'z') {
      removePoint();
    }
    else if (key == '-') {
      speed-=0.01f;
      println("Speed : "+speed);
    }
    else if (key == '=') {
      speed+=0.01f;
      println("Speed : "+speed);
    }
    else if (PApplet.parseFloat(key)>=48&&PApplet.parseFloat(key)<=57) {
      mode = PApplet.parseInt(key)-48;
      println("Mode #"+mode);
    }
    else if (key == 'h') {
      println("b break line");
      println("h help");
      println("m mouse");
      println("r reverse direction");
      println("t trails");
      println("z undo");
      println("0-9 modes");
      println("- speed--");
      println("= speed++");
    }
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--hide-stop", "mipmap_002" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

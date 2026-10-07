// Firmware contoh mobil robot 4 roda
// Sesuaikan pin dengan wiring Anda.
// Protokol serial: FORWARD, BACKWARD, LEFT, RIGHT, STOP
const int ENA=5, IN1=7, IN2=8;
const int ENB=6, IN3=9, IN4=10;

void setup(){
  Serial.begin(115200);
  pinMode(ENA,OUTPUT); pinMode(IN1,OUTPUT); pinMode(IN2,OUTPUT);
  pinMode(ENB,OUTPUT); pinMode(IN3,OUTPUT); pinMode(IN4,OUTPUT);
  stopRobot();
}

void loop(){
  if(Serial.available()){
    String cmd=Serial.readStringUntil('\n');
    cmd.trim();
    if(cmd=="FORWARD") forward(180);
    else if(cmd=="BACKWARD") backward(180);
    else if(cmd=="LEFT") left(160);
    else if(cmd=="RIGHT") right(160);
    else stopRobot();
  }
}

void setMotor(int en,int inA,int inB,int pwm,bool forwardDir){
  digitalWrite(inA,forwardDir?HIGH:LOW);
  digitalWrite(inB,forwardDir?LOW:HIGH);
  analogWrite(en,constrain(pwm,0,255));
}
void forward(int pwm){setMotor(ENA,IN1,IN2,pwm,true);setMotor(ENB,IN3,IN4,pwm,true);}
void backward(int pwm){setMotor(ENA,IN1,IN2,pwm,false);setMotor(ENB,IN3,IN4,pwm,false);}
void left(int pwm){setMotor(ENA,IN1,IN2,0,true);setMotor(ENB,IN3,IN4,pwm,true);}
void right(int pwm){setMotor(ENA,IN1,IN2,pwm,true);setMotor(ENB,IN3,IN4,0,true);}
void stopRobot(){analogWrite(ENA,0);analogWrite(ENB,0);}
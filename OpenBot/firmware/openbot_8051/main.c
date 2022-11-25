/* C51 for OpenBot 1.22*/
//���ڷֱ����2��pwm 	
//�д����� �������Ӳ�����⵼����������ٶȲ�һ��	   
//��ʱ�俴�� ֱ��������� pwm �� pid �㷨	https://blog.csdn.net/luzire/article/details/83144381
#include <reg52.h>
#include"uart.h"
#define HIGH 1		  //threshold value ��ֵ
#define LOW -HIGH		  //threshold value ��ֵ
#define HZ 100	// ���� Լ0.1ms һ���ж�

//L293D���Ŷ���      
sbit in3 = P1^2;	 //������ҵ��λ�ò��ԣ�34 �� 12 ����λ��
sbit in4 = P1^3;
sbit in1 = P1^4;
sbit in2 = P1^5;
//sbit ena = P0^7; 	 //	������û������˳��������˵��ô�ֱ����2��pwmһֱ�����ԣ��������ԣ��»���������Ҳ�����
//sbit enb = P0^6;     // ����˳���ˣ��Ű����Ѿ��������ϱ��ˣ����û�λ����   
sbit ena = P1^6;	//���Ż��˸�λ��
sbit enb = P1^7;
 
int ctrl_left  = 0;
int ctrl_right = 0;
uint PWMA = 20;         
uint PWMB = 20; 
unsigned char MA = 0,MB = 0;           //pwm������

void delay(unsigned int n){ while (n--);}


//void ConfigPWM(long cl, long cr)
//{
//	PWMA = cl;
//	PWMB = cr;
//}

void update_right_motors()
{
  if (ctrl_right < LOW)
  {
	if(ctrl_right < -255) ctrl_right = -255;//����Χ���ݴ���
  	in3=1;  
	in4=0;
	PWMB = -ctrl_right;
	
  }
  else if (ctrl_right > HIGH)
  {
  	if(ctrl_right > 255) ctrl_right = 255;//����Χ���ݴ���
    in3=0;  
	in4=1;
	PWMB = ctrl_right;
  }
  else
  {	//stop_left_motors
    in3=0;  
    in4=0;
  }
}


void update_left_motors()
{
  if (ctrl_left < LOW)
  {
	if(ctrl_left < -255) ctrl_left = -255;//����Χ���ݴ���
    in1=1;  
	in2=0;
	PWMA = -ctrl_left;
  }
  else if (ctrl_left > HIGH)
  {
  	if(ctrl_left > 255) ctrl_left = 255;//����Χ���ݴ���
    in1=0;  
	in2=1;
	PWMA = ctrl_left;
  }
  else
  {
    //stop_right_motors
	in1=0;  
    in2=0;
  }
}

//������ 
void main()
{   
	EA=1;
    ConfigUART(9600);  //���ò�����Ϊ9600
	Uart_Send_String("8051 for OpenBot 1.22 \r\n");//OpenBot�ֻ��˽���δ�涨������ͷ�ᱨ��,�ҽ�����ֻ���USB�����쳣���ݵĴ���Ҳ���԰�����Ĵ������ע�͵�
	in1=1;  
	in2=1;
	in3=1;  
	in4=1;
    while (1)
    {
//	  ConfigPWM(ctrl_left,ctrl_right);//pwm����	 

	  update_left_motors();
   	  update_right_motors();

//	  Uart_Send_String("PWMA:");
//	  Uart_Send_Byte(PWMA/100+0x30);	//��λ
//	  Uart_Send_Byte((PWMA-PWMA/100*100)/10+0x30);	 //ʮλ
//	  Uart_Send_Byte(PWMA%10+0x30);					//	 ��λ
//	  Uart_Send_String("\r\n");
	delay(2000);
	}
}
 

void InterruptTimer0() interrupt 1
{
TR0 = 0;  
			    
	TH0 = (65536-HZ)/256;			   //65536 = ffff = 16λ
	TL0 = (65536-HZ)%256;			   //256 ff 8λ

MB++;         
if(MB < PWMB)  
{   
enb = 1;                             //ʹ��enb������pwm������B�˵��
}  
else
  enb = 0;  
if(MB == 255)
{   
  MB = 0;  
}

MA++;         
if(MA < PWMA)  
{   
ena = 1;                                 //ʹ��ena������pwm������A�˵��
}  
else
  ena = 0;  
if(MA == 255)
{   
  MA = 0;  
}  

TR0 = 1;

}

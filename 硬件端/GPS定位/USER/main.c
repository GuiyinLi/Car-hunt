#include "stm32f10x.h"
#include "sys.h"
#include "led.h"
#include "delay.h"
#include "usart2.h"
#include "usart.h"
#include "gps.h"
#include "gsm.h"
#include "usart3.h"
nmea_msg gpsx; 											//GPS信息
int main()
{
	u16 i,rxlen;
	delay_init();	    	 //延时函数初始化	
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2); //设置NVIC中断分组2:2位抢占优先级，2位响应优先级
	uart_init(115200);	 	//电脑测试
	USART2_Init(115200);  //GPRS通信
	USART3_Init(9600);    //GPS通信
	LED_Init();
	led=1;
	Init();
	while(1)
	{
		if(USART3_RX_STA&0X8000)		//接收到一次数据了
		{
			rxlen=USART3_RX_STA&0X7FFF;	//得到数据长度	   
			USART3_TX_BUF[rxlen]=0;			//自动添加结束符
			GPS_Analysis(&gpsx,(u8*)USART3_RX_BUF);//分析字符串
			Gps_Get_Send_Data();		               //获取并发送信息
			for(i=0;i<1000;i++)
    		USART3_RX_BUF[i]=0;
			USART3_RX_STA=0;		   	//启动下一次接收
			led=!led;
 		}
	}
}





using System;
using System.Net.Http;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Timers;
using System.Net;

namespace WindowsFormsApp1
{
    public partial class Form1 : Form
    {
        private static readonly HttpClient client = new HttpClient();
        private System.Timers.Timer dataTimer;

        public Form1()
        {
            InitializeComponent();
            SetupTimer();
        }

        private void SetupTimer()
        {
            dataTimer = new System.Timers.Timer(1000); // 1초마다 실행
            dataTimer.Elapsed += new ElapsedEventHandler(OnTimedEvent);
            dataTimer.AutoReset = true;
            dataTimer.Enabled = true;
        }

        private async void OnTimedEvent(object source, ElapsedEventArgs e)
        {
            await UpdateSensorData();
        }

        private async Task UpdateSensorData()
        {
            try
            {
                string ledData = await client.GetStringAsync("http://localhost:8080/arduino/message/led");
                string tmpData = await client.GetStringAsync("http://localhost:8080/arduino/message/tmp");
                string lightData = await client.GetStringAsync("http://localhost:8080/arduino/message/light");
                string distanceData = await client.GetStringAsync("http://localhost:8080/arduino/message/distance");

                // UI 스레드에서 TextBox 업데이트
                this.Invoke((MethodInvoker)delegate {
                    ledBox1.Text = ledData;
                    tempBox1.Text = tmpData;
                    lightBox1.Text = lightData;
                    ultrasonicBox1.Text = distanceData;
                });
            }
            catch (Exception ex)
            {
                Console.WriteLine("Exception: " + ex.Message);
            }
        }

        private void conn_btn_Click(object sender, EventArgs e)
        {
            String port = this.comboBox1.Items[this.comboBox1.SelectedIndex].ToString();
            Console.WriteLine("PORT : " + port);
            HttpWebRequest request = null;
            HttpWebResponse response = null;
            try
            {
                request = (HttpWebRequest)HttpWebRequest.Create("http://localhost:8080/arduino/connection/" + port);
                request.Method = "GET";
                request.ContentType = "application/json";

                response = (HttpWebResponse)request.GetResponse();

                if (response.StatusCode == HttpStatusCode.OK)
                {
                    Console.WriteLine("RESPONSE CODE : " + response.StatusCode);
                }

            }
            catch (Exception ex)
            {
                Console.WriteLine("Ex : " + ex);
            }
        }

        private void led_on_btn_Click(object sender, EventArgs e)
        {
            HttpWebRequest request = (HttpWebRequest)HttpWebRequest.Create("http://localhost:8080/arduino/led/1");
            request.Method = "GET";
            request.ContentType = "application/json";
            HttpWebResponse response = (HttpWebResponse)request.GetResponse();
        }

        private void led_off_btn_Click(object sender, EventArgs e)
        {
            HttpWebRequest request = (HttpWebRequest)HttpWebRequest.Create("http://localhost:8080/arduino/led/0");
            request.Method = "GET";
            request.ContentType = "application/json";
            HttpWebResponse response = (HttpWebResponse)request.GetResponse();
        }
    }
}

// See https://aka.ms/new-console-template for more information

using System.Text.Json;
using TbsSdk;
using TbsSdk.Configuration;

TbsSdk.TbsSdkClient client = new TbsSdkClient(new TbsSdkOptions() { BaseUrl = "http://192.168.80.32:8082" });
var token = client.LoginAsync("01505", "01505");
var r = token.Result;

Console.WriteLine(JsonSerializer.Serialize(r));
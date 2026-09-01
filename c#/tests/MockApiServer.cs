using System.Net;
using System.Net.Sockets;
using System.Text;

namespace CommsTests;

/// <summary>
/// Minimal local HTTP server used to mock CommsSdk's outgoing API calls in tests,
/// since HttpClient isn't injectable in the SDK. Point the SDK at <see cref="Url"/>
/// via <c>CommsSdk.UseCustomServer</c>, queue canned responses, then inspect
/// <see cref="RequestBodies"/> to assert what the SDK actually sent.
/// </summary>
public sealed class MockApiServer : IDisposable
{
    private readonly HttpListener _listener;
    private readonly Queue<(int Status, string Body)> _responses = new();

    public List<string> RequestBodies { get; } = new();
    public string Url { get; }

    public MockApiServer()
    {
        var port = GetFreePort();
        Url = $"http://127.0.0.1:{port}/";
        _listener = new HttpListener();
        _listener.Prefixes.Add(Url);
        _listener.Start();
        _ = Task.Run(HandleRequestsAsync);
    }

    public void EnqueueResponse(string jsonBody, int statusCode = 200)
    {
        lock (_responses) _responses.Enqueue((statusCode, jsonBody));
    }

    private async Task HandleRequestsAsync()
    {
        while (_listener.IsListening)
        {
            HttpListenerContext ctx;
            try
            {
                ctx = await _listener.GetContextAsync();
            }
            catch (Exception)
            {
                break; // listener was stopped/disposed
            }

            using (var reader = new StreamReader(ctx.Request.InputStream, Encoding.UTF8))
            {
                var body = await reader.ReadToEndAsync();
                lock (RequestBodies) RequestBodies.Add(body);
            }

            (int Status, string Body) response;
            lock (_responses)
            {
                response = _responses.Count > 0 ? _responses.Dequeue() : (200, "{\"Status\":\"OK\"}");
            }

            var bytes = Encoding.UTF8.GetBytes(response.Body);
            ctx.Response.StatusCode = response.Status;
            ctx.Response.ContentType = "application/json";
            ctx.Response.ContentLength64 = bytes.Length;
            await ctx.Response.OutputStream.WriteAsync(bytes);
            ctx.Response.OutputStream.Close();
        }
    }

    public void Dispose()
    {
        _listener.Stop();
        _listener.Close();
    }

    private static int GetFreePort()
    {
        var listener = new TcpListener(IPAddress.Loopback, 0);
        listener.Start();
        var port = ((IPEndPoint)listener.LocalEndpoint).Port;
        listener.Stop();
        return port;
    }
}

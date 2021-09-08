package edu.eci.arep;

import java.net.*;
import java.io.*;
import java.util.HashMap;
public class HttpServer {
    public HttpServer(){}

    /*
     *   Método que inicializa el servidor
     * */
    public void start() throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println( "Listo para recibir ..." );
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println( "Accept failed." );
                System.exit( 1 );
            }
            serveConnection(clientSocket);
            clientSocket.close();

        }
        serverSocket.close();
    }

    /**
     * Metodo  procesar las peticiones.
     * @param clientSocket conexion Socket con cliente
     * @throws IOException Error de lectura del cliente socket
     */
    public void serveConnection (Socket clientSocket) throws IOException {
        OutputStream out= clientSocket.getOutputStream();
        BufferedReader in = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
        String inputLine, outputLine;
        HashMap<String,String> request  = new HashMap<String,String>();
        boolean ready = false;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);
            if(!ready){
                request.put("rq", inputLine);
                ready =true;
            } else {
                String[] line = inputLine.split( ":" );
                if (line.length>1)
                    request.put( line[0], line[1] );
            }
            if (!in.ready()) {
                break;
            }
        }
        getResource(request,out);
        in.close();
    }

    /**
     * Genera la respuesta de la peticion del cliente
     * @param ClientSocket OutputStream del socket del cliente
     * @param response información procesada de la petición del cliente
     * @throws IOException Error de escritura del recurso
     */
    public  void getResource(HashMap<String,String> response, OutputStream ClientSocket) throws IOException {
        String outputLine = "";
        if (response.get("rq")!= null) {
            PrintWriter printWriter = new PrintWriter( ClientSocket, true );
            String[] lineRequest = response.get( "rq" ).split( " " );
            String path = lineRequest[1];
            if ( path.equals( "/" ) ) path = "/index.html";
            byte pathByte[] = findResource( path );
            if ( pathByte != null ) {
                outputLine = generateHeader( path );
                printWriter.println( outputLine );
                ClientSocket.write( pathByte );
            }
        }
        ClientSocket.close();
    }

    /**
     * Metodo que obtiene un recurso almacenado.
     * @param path nombre ruta del recurso
     * @return arreglo de bytes del recurso para el outputStream
     * @throws IOException error de lectura del archivo
     */
    private byte[] findResource(String path) throws IOException {
        String dir = "src/main/resources/html_public" +  path;
        File resource= new File(dir);
        byte content[] = new byte[(int)resource.length()];
        if (resource.exists() && resource.isFile() ){
            FileInputStream fileStream = new FileInputStream(resource);
            fileStream.read(content);
            fileStream.close();
        }else{
            content = null;
        }
        return content;
    }

    /**
     * Genera encabezado segun el tipo de recurso solicitado
     * @param path ruta del recurso
     * @return Encabezado string http
     */
    private String generateHeader(String path) {
        String header = "";
        String[] list = path.split("\\.");
        String type = list[list.length-1];
        if( type.equals( "jpg" ) || type.equals( "png" ) ){
            header += "HTTP/1.1 200 OK\r\n Content-Type: image/" + type +"\r\n";
        }else if (type.equals( "html" ) || type.equals( "js" ) || type.equals( "css" )){
            header += "HTTP/1.1 200 OK\r\n Content-Type: text/" + type +"\r\n" + "\r\n";
        }
        return header;
    }
}
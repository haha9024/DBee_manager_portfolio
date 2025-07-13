package com.manager.dbee.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manager.dbee.dao.policy.PolicyDAO;

@Service
public class PolicyReloadService {
	
	private static final org.slf4j.Logger logger =
	        org.slf4j.LoggerFactory.getLogger(PolicyReloadService.class);
		
	@Autowired
	private PolicyDAO policyDAO;
	private final DatagramSocket udpSocket; 
	private final InetAddress addr; 
	private final int port;
	
	private static final String HOST = "192.168.1.24";
	private static final int PORT = 9090;
	
	public PolicyReloadService(PolicyDAO policyDAO) {
		this.policyDAO = policyDAO;
			
		// 1) InetAddress 생성
		try {
			this.addr = InetAddress.getByName(HOST);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("호스트 해석 실패: " + HOST, e);
		}
		this.port = PORT;
		
		
		// 2) DatagramSocket 생성
		try {
			this.udpSocket = new DatagramSocket();
		} catch(BindException e) {
			throw new IllegalStateException("UDP 포트 바인딩 실패: " + port, e);
		} catch (SocketException e) {
            throw new IllegalStateException("UDP 소켓 생성 실패", e);
        }
		
	}
	
	public void sendReloadSignal() {
		
		// 1) 보낼 데이터
		byte[] alert = "reload".getBytes();
		
		
		// 2) UDP 패킷 생성
		DatagramPacket signal = new DatagramPacket(alert, alert.length, addr, port);
		
		
		// 3) UDP 소켓 송신
		try {		
			udpSocket.send(signal);
			logger.info("reload 신호 전송: {}:{}", HOST, port);			
		} catch (PortUnreachableException e) {
			throw new IllegalStateException(
					"UDP 대상이 응답하지 않습니다 (port unreachable): " + port, e);
		} catch (IOException e) {
			throw new UncheckedIOException("UDP 패킷 전송 중 I/O 예외가 발생했습니다", e);
		} catch (SecurityException e) {
			// 보안 매니저가 허용하지 않을 때
			throw new SecurityException("UDP 전송 권한이 없습니다: " + HOST + ":" + port, e);
		}
		
		
		// 4) UDP 소켓 종료
		udpSocket.close();
	} 
	
	
	//public static void sendReloadSignal()
	/*
	 * public void reload(Policy policy) {
	 * sendReloadSignal(policy.getUpdated_at().toLocalDateTime()); }
	 * 
	 * private void sendReloadSignal(LocalDateTime since) { 
	 * Map<String,Object> payload = Map.of( "action", "reload", "since",
	 * since.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ); 
	 * 
	 * try { 
	 * 	byte[] buf = new ObjectMapper().writeValueAsBytes(payload);
	 * 	DatagramPacket pkt = new DatagramPacket(buf, buf.length, addr, port);
	 * 	udpSocket.send(pkt); 
	 * } catch
	 * (PortUnreachableException e) { 
	 * 	logger.warn("UDP 대상 포트가 열려 있지 않습니다: {}", e.getMessage());
	 *  } catch (IOException e) {
	 * 	throw new UncheckedIOException("UDP 전송 실패", e);
	 *  } 
	 * }
	 */
}

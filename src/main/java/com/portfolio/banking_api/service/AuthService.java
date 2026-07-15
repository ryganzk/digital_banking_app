package com.portfolio.banking_api.service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JWTService jwtService;

    public AuthService(UserRepository userRepository, JWTService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("User already exists");
        }
        User user = new User(request.email(), passwordEncoder.encode(request.password()), request.firstName(), request.middleName(), request.lastName(), "CUSTOMER");
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new RuntimeException("User not found"));
        return new AuthResponse(jwtService.generateToken(user));
    }
}
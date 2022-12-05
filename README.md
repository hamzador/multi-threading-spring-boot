# multi-threading-spring-boot
url for get files for test: https://mockaroo.com/
  ![image](https://user-images.githubusercontent.com/39261811/205683939-283868d1-79d5-4d2c-863b-9ad47fbfafa4.png)
# Class of configuration
		
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.scheduling.annotation.EnableAsync;
	import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

	import java.util.concurrent.Executor;

	@Configuration
	@EnableAsync
	public class AsyncConfig {

			@Bean(name= "taskExecutor")
			public Executor taskExecutor(){
					ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
					executor.setCorePoolSize(2);
					executor.setMaxPoolSize(2);
					executor.setQueueCapacity(100);
					executor.setThreadNamePrefix("userThhread-");
					executor.initialize();
					return executor;
			}
	}

# User Class Entity

	@Data
	@Entity
	@AllArgsConstructor @NoArgsConstructor @Builder
	@Table(name = "USER_Tbl")
	public class User {
			@Id
			@GeneratedValue(strategy = GenerationType.IDENTITY)
			private Long id;
			private String first_name;
			private String last_name;
			private String email;
			private String gender;
	}

# Repository 
	
	public interface UserRepository extends JpaRepository<User, Long> {}

# Service 

	@Service
	public class UserService {
			@Autowired
			private UserRepository userRepository;

			Object target;
			Logger logger =  LoggerFactory.getLogger(UserService.class);

			@Async
			public CompletableFuture <List<User>> saveUsers(MultipartFile multipartFile) throws Exception {
					long start = System.currentTimeMillis();
					List<User> users = parseCVC(multipartFile);
					logger.info("saving list of users of size {}", users.size(), "" + Thread.currentThread().getName());
					users = userRepository.saveAll(users);
					long end = System.currentTimeMillis();
					logger.info("Total time {}", (end - start));
					return CompletableFuture.completedFuture(users);
			}

			@Async
			public CompletableFuture<List<User>> findAllUsers(){
					logger.info("get list of user by " + Thread.currentThread().getName());
					List<User> users = userRepository.findAll();
					return CompletableFuture.completedFuture(users);
			}

			private List<User> parseCVC(final MultipartFile file) throws Exception {
					final List<User> users = new ArrayList<>();
					try {
							try(final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
									String line;
									while ((line = br.readLine()) != null) {
											final String[] data = line.split(",");
											final User user = new User();
											user.setFirst_name(data[0]);
											user.setLast_name(data[1]);
											user.setEmail(data[2]);
											user.setGender(data[3]);
											users.add(user);
									}
									return users;
							}
					}catch (final IOException e){
							logger.error("Failed to parse CSV file {}", e);
							throw new Exception("Failed to parse CSV file {}", e);
					}

			}

	}
	
# Controller class:
	
	@RestController
	public class UserController {
			@Autowired
			private UserService service;

			@PostMapping(value = "users", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
			public ResponseEntity saveUser(@RequestParam(value = "files") MultipartFile[] files) throws Exception {
					for (MultipartFile file: files){
							service.saveUsers(file);
					}
					return ResponseEntity.status(HttpStatus.CREATED).build();
			}

			@GetMapping(value = "/users", produces = "application/json")
			public CompletableFuture<ResponseEntity> findAllUsers(){
					return service.findAllUsers().thenApply(ResponseEntity::ok);
			}

			@GetMapping(value = "/usersByMultiThread", produces = "application/json")
			public ResponseEntity getUsers(){
					CompletableFuture<List<User>> users1 = service.findAllUsers();
					CompletableFuture<List<User>> users2 = service.findAllUsers();
					CompletableFuture<List<User>> users3 = service.findAllUsers();
					CompletableFuture.allOf(users1, users2, users3).join();
					return ResponseEntity.status(HttpStatus.OK).build();
			}
	}

# application.properties file: 

server.port=9000

	spring.datasource.url=jdbc:mysql://localhost:3306/multi-tread?createDatabaseIfNotExist=true&userSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC
	spring.datasource.username=root
	spring.datasource.password=
	spring.jpa.hibernate.ddl-auto=update
	spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

	## Hibernate Properties
	# The SQL dialect makes Hibernate generate better SQL for the chosen database
	spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.MySQL5Dialect

	
	
	
# Result:
![image](https://user-images.githubusercontent.com/39261811/205685012-20d3c9b7-1b7f-4358-84d1-aef96db0318d.png)


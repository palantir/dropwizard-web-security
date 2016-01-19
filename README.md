dropwizard-web-security
=======================
A bundle for applying default web security functionality to a dropwizard application. It covers the following areas:

- CORS
- Web Application Security Headers (Content Security Policy, etc.)
- HTTP STS

Usage
-----
1. Ensure your configuration implements `WebSecurityConfigurable`.
2. Add the bundle to your application.

	```java
	public class ExampleApplication extends Application<ExampleConfiguration> {
	
	    @Override
	    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
	        bootstrap.addBundle(new WebSecurityBundle());
	    }
	    
	    // ...
	}
	```
3. Make sure to modify your YAML configuration to include your `allowedOrigins`. **By default, no origins are allowed.**


Configuration
-------------
Add configuration for the bundle to your application's Dropwizard configuration file. A minimal example looks
like the following:

```yaml
webSecurity:
    cors:
        allowedOrigins: "http://localhost"
```

The above minimal configuration will yield the following behaviors:

- CORS enabled, allowing the `http://localhost` origin
- The default App Security headers will be injected
- HSTS is turned OFF by default


### CORS Configuration
You can configure the CORS section by using the following YAML values:

Field | Default
----- | -------
`enabled` | `true`
`allowCredentials` | `true`
`allowedHeaders` | `"X-Requested-With,Content-Type,Accept,Origin"`
`allowedMethods` | `"GET,POST,HEAD"`
`allowedOrigins` | `""` *(No origins allowed by default)*
`exposedHeaders` | `""`
`preflightMaxAge` | `1800`

**NOTE:** The values shown are from [`CrossOriginFilter`][1], except the following:

- `enabled` - CORS is on by default
- `allowedOrigins` - Set to blank instead of `"*"` to require the user to enter the allowed origins.

If a value is not configured, it will not be passed along to the `CrossOriginFilter`.


### App Security Configuration
You can configure the App Security section by using the following YAML values:

Field | Default
----- | -------
`enabled` | `true`
contentSecurityPolicy | `"default-src 'self'"`
contentTypeOptions | `"nosniff"`
frameOptions | `"sameorigin"`
xssProtection | `"1; mode=block"`


### HSTS Configuration
You can configure the HSTS section by using the following YAML values:

Field | Default
----- | -------
`enabled` | `false`
`headerValue` | `""`


Advanced Usage
--------------
You can customize your application's defaults by defining it inside of your `initialize` method. Any value not set will
be set to the default values.

**Note:** the application default values will be **over-riden by the YAML defined values**.

```java
public class AdvancedApplication extends Application<AdvancedConfiguration> {
    @Override
    public void initialize(Bootstrap<AdvancedConfiguration> bootstrap) {

        // define the application defaults
        WebSecurityConfiguration applicationDefaults = new WebSecurityConfiguration.Builder()

                .cors(new CorsConfiguration.Builder()
                        .allowedHeaders("Origin,Content-Type,Accept")
                        .preflightMaxAge(60 * 10)
                        .build())

                .appSecurity(new AppSecurityConfiguration.DISABLED)

                .hsts(new HstsConfiguration.Builder()
                        .enabled(true)
                        .headerValue("max-age=31536000; includeSubDomains")
                        .build()

                .build();

        // apply the bundle with the application defaults
        bootstrap.addBundle(new WebSecurityBundle(applicationDefaults));
    }
}
```


License
-------
This project is made available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0).

[1]: http://download.eclipse.org/jetty/9.2.13.v20150730/apidocs/org/eclipse/jetty/servlets/CrossOriginFilter.html

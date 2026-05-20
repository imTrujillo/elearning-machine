<?php
/**
 * Plugin Name: Allow Application Passwords on HTTP (local)
 * Description: Necesario para que learning-engine autentique contra WordPress en http://localhost:8080.
 */

if (!defined('ABSPATH')) {
    exit;
}

add_filter('wp_is_application_passwords_available', '__return_true');

if (!defined('WP_ENVIRONMENT_TYPE')) {
    define('WP_ENVIRONMENT_TYPE', 'local');
}

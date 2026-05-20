# Sin descargas en build (la VPS a veces no resuelve downloads.wordpress.org).
# WooCommerce: instalar desde WP Admin → Plugins → Añadir nuevo.

FROM wordpress:6.7-php8.2-apache

COPY mu-plugins/ /usr/src/wordpress/wp-content/mu-plugins/
COPY apache-wordpress.conf /etc/apache2/conf-available/wordpress-override.conf

RUN a2enmod rewrite \
    && a2enconf wordpress-override

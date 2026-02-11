// Load GSAP from CDN - Mintlify auto-includes .js files from content root
const script = document.createElement('script');
script.src = 'https://cdnjs.cloudflare.com/ajax/libs/gsap/3.12.5/gsap.min.js';
script.onload = () => {
  window.gsapLoaded = true;
  window.dispatchEvent(new Event('gsap-ready'));
};
document.head.appendChild(script);
